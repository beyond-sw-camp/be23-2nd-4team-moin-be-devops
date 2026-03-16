package com.team4.moin.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.moin.chat.domain.*;
import com.team4.moin.chat.dtos.ChatImageDto;
import com.team4.moin.chat.dtos.ChatMessageDto;
import com.team4.moin.chat.dtos.MessageUpdateDto;
import com.team4.moin.chat.repository.ChatMessageRepository;
import com.team4.moin.chat.repository.ChatParticipantRepository;
import com.team4.moin.chat.repository.ChatRoomRepository;
import com.team4.moin.chat.repository.ReadStatusRepository;
import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final RedisPubSubService redisPubSubService;
    private final ObjectMapper objectMapper;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository,
                              ReadStatusRepository readStatusRepository, UserRepository userRepository,
                              RedisPubSubService redisPubSubService, ObjectMapper objectMapper, S3Client s3Client, S3Presigner s3Presigner) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
        this.redisPubSubService = redisPubSubService;
        this.objectMapper = objectMapper;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Value("${aws.s3.bucket3}")
    private String bucket3;

    public void saveMessage(Long roomId, ChatMessageDto dto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 보낸사람 조회 (Email로 User 조회 후 참여자 확인)
        User user = userRepository.findAllByEmailWithAddress(dto.getSenderEmail())
                .orElseThrow(() -> new EntityNotFoundException("user cannot be found"));

        // 해당 채팅방의 참여자 목록 조회
//        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomWithCrewMemberAndUser(chatRoom);
        CrewMember sender = chatParticipants.stream()
                .map(ChatParticipant::getCrewMember)
                .filter(crewMember -> crewMember.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("크루 멤버 없음 (해당 채팅방 참여자가 아닙니다.)"));

        if (dto.getMessageType() == MessageType.NOTICE) {
            // OWNER나 MANAGER가 아니면 예외 발생
            if (sender.getRole() != CrewRole.OWNER && sender.getRole() != CrewRole.MANAGER) {
                throw new IllegalArgumentException("공지사항은 방장(OWNER) 또는 운영진(MANAGER)만 작성할 수 있습니다.");
            }
        }

        saveAndPublishMessage(chatRoom, sender, dto.getMessage(), dto.getMessageType(), null, chatParticipants);
    }

    // URL 발급 메서드
    public String getPresignedUrl(String originalFileName) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = "chatImages-" + user.getId() + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket3)
                .key(fileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // URL 유효시간 5분
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

//    // 사진 메시지 전송
    public void saveChatImages(ChatImageDto dto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId()).orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 보낸사람 조회
        User user = userRepository.findAllByEmail(dto.getSenderEmail()).orElseThrow(() -> new EntityNotFoundException("user cannot be found"));

        // 해당 채팅방의 참여자 목록 조회
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomWithCrewMemberAndUser(chatRoom);
        CrewMember sender = chatParticipants.stream()
                .map(ChatParticipant::getCrewMember)
                .filter(crewMember -> crewMember.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("크루 멤버 없음 (해당 채팅방 참여자가 아닙니다.)"));

        if (dto.getImages() != null && dto.getImages().size() > 10) {
            throw new IllegalArgumentException("사진 전송은 10장 미만까지");
        }

        List<String> imgUrls = new ArrayList<>();

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            imgUrls.addAll(dto.getImages());
        }

        saveAndPublishMessage(chatRoom, sender, dto.getMessage(), dto.getMessageType(), imgUrls, chatParticipants);
    }

    public void updateMessage(MessageUpdateDto dto) {
        // 이미 삭제 및 수정한 메시지는 중복 추가 수정 불가
//        사진 전송한 메시지도 수정 불가하도록 추가.
        ChatMessage chatMessage = chatMessageRepository.findByIdAndDelYn(dto.getMessageId(), "N")
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다."));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!chatMessage.getCrewMember().getUser().getEmail().equals(currentUserEmail)) {
            throw new IllegalArgumentException("본인의 메시지만 수정할 수 있습니다.");
        }

        if (chatMessage.getDelYn().equals("Y")) {
            throw new IllegalStateException("삭제된 메시지는 수정할 수 없습니다.");
        }

        chatMessage.updateContent(dto.getNewContent());

        ChatMessageDto updateDto = ChatMessageDto.builder()
                .messageId(chatMessage.getId())
                .roomId(dto.getRoomId())
                .message(chatMessage.getContent())
                .senderEmail(chatMessage.getCrewMember().getUser().getEmail())
                .senderNickname(chatMessage.getCrewMember().getUser().getNickname())
                .senderProfileImage(chatMessage.getCrewMember().getUser().getProfileImageUrl())
                .delYn(chatMessage.getDelYn())
                .editedYn("Y")
                .messageType(chatMessage.getMessageType())
                .build();
        System.out.println("update roomId: " + dto.getRoomId());

        try {
            String jsonMessage = objectMapper.writeValueAsString(updateDto);
            redisPubSubService.publish("chat", jsonMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deleteMessage(Long messageId) {
        // 이미 삭제한 메시지는 삭제 불가
        ChatMessage chatMessage = chatMessageRepository.findByIdAndDelYn(messageId, "N")
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다."));

        // 삭제 권한 확인 (현재 로그인한 유저가 보낸 메시지인지)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!chatMessage.getCrewMember().getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("본인의 메시지만 삭제할 수 있습니다.");
        }

        if ("Y".equals(chatMessage.getDelYn())) {
            throw new IllegalStateException("이미 삭제된 메시지입니다.");
        }

        chatMessage.delete();

        ChatMessageDto deleteDto = ChatMessageDto.builder()
                .messageId(chatMessage.getId())
                .roomId(chatMessage.getChatRoom().getId())
                .message("삭제된 메시지입니다.")
                .senderEmail(email)
                .senderNickname(chatMessage.getCrewMember().getUser().getNickname())
                .delYn("Y")
                .editedYn(chatMessage.getEditedYn())
                .messageType(MessageType.NORMAL)
                .build();

        try {
            String messageJson = objectMapper.writeValueAsString(deleteDto);
            redisPubSubService.publish("chat", messageJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void sendEnterMessage(ChatRoom chatRoom, CrewMember crewMember) {

        if (chatRoom.getIsGroupChat().equals("N")){
            return;
        }
        List<ChatParticipant> chatParticipants =
                chatParticipantRepository.findByChatRoomWithCrewMemberAndUser(chatRoom);
        saveAndPublishMessage(chatRoom, crewMember, crewMember.getUser().getNickname() + "님이 입장하였습니다.", MessageType.ENTER, null, chatParticipants);
    }

    // 입장 시점 기록용 최신 messageId 조회
    public Long getLatestMessageId(ChatRoom chatRoom) {
        return chatMessageRepository.findLatestMessageIdByChatRoom(chatRoom).orElse(null);
    }

    // 채팅 메시지 내용 확인
    public List<ChatMessageDto> getChatHistory(Long roomId) {
        // 내가 해당 채팅방의 참여자가 아닐 경우 에러
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("없는 방입니다."));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ChatParticipant chatParticipant = chatParticipantRepository.findByRoomIdAndUserEmail(roomId, email)
                .orElseThrow(() -> new IllegalArgumentException("본인이 속하지 않은 채팅방입니다."));

        if (chatParticipant.getStatus() != ChatParticipantStatus.JOINED) {
            throw new IllegalArgumentException("접근 권한이 없는 채팅방입니다.");
        }

        List<ChatMessage> messages = new ArrayList<>();
        Long startMessageId = chatParticipant.getStartMessageId();

        if (startMessageId == null) {
            messages = chatMessageRepository.findByChatRoomWithCrewMemberAndUser(chatRoom);
        } else {
            messages = chatMessageRepository.findByChatRoomWithCrewMemberAndUserAfterMessageId(chatRoom, startMessageId);
        }

        List<Long> messageIds = messages.stream().map(ChatMessage::getId).toList();
        List<Object[]> unreadCounts = readStatusRepository.countByMessageIdsAndIsRead(messageIds, false);

        Map<Long, Long> unreadCountMap = unreadCounts.stream().collect(Collectors.toMap(
                arr -> (Long) arr[0],
                arr -> (Long) arr[1]));

        return messages.stream().map(c -> {
            // 탈퇴한 사용자 닉네임 치환
            String senderEmail;
            String senderNickname;
            ChatParticipant senderParticipant = chatParticipantRepository
                    .findByChatRoomAndCrewMember(chatRoom, c.getCrewMember())
                    .orElse(null);

            if (senderParticipant == null || senderParticipant.getStatus() == ChatParticipantStatus.LEAVED) {
                senderEmail = "unknown";
                senderNickname = "unknown";
            } else {
                senderEmail = c.getCrewMember().getUser().getEmail();
                senderNickname = c.getCrewMember().getUser().getNickname();
            }

            return ChatMessageDto.builder()
                    .messageId(c.getId())
                    .roomId(roomId)
                    .message(c.getContent())
                    .senderEmail(senderEmail)
                    .senderNickname(senderNickname)
                    .senderProfileImage(senderParticipant == null || senderParticipant.getStatus() == ChatParticipantStatus.LEAVED
                            ? null : c.getCrewMember().getUser().getProfileImageUrl())
                    .messageType(c.getMessageType())
                    .delYn(c.getDelYn())
                    .editedYn(c.getEditedYn())
//                    .imageUrls(c.getImgUrls())
                    .imageUrls(new ArrayList<>(c.getImgUrls()))
                    .unreadCount(unreadCountMap.getOrDefault(c.getId(), 0L).intValue())
                    .build();
        }).toList();
    }

    //   메시지 저장 + 읽음 상태 저장 + Redis 발행을 담당하는 공통 메서드
    private void saveAndPublishMessage(ChatRoom chatRoom, CrewMember sender, String content, MessageType messageType, List<String> imgUrls,
                                       List<ChatParticipant> chatParticipants) {

        if ("N".equals(chatRoom.getIsGroupChat())) {
            Long latestMessageId = getLatestMessageId(chatRoom); // c 저장 전 시점 = b의 id
            for (ChatParticipant p : chatParticipants) {
                if (p.getStatus() == ChatParticipantStatus.HIDDEN) {
                    p.restoreFromHidden(latestMessageId);
                    if (latestMessageId != null) {
                        readStatusRepository.markAsReadUpTo(p.getCrewMember(), chatRoom, latestMessageId);
                    }
                }
            }
        }


        // 1. 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .crewMember(sender)
                .content(content)
                .messageType(messageType)
                .imgUrls(imgUrls)
                .build();
        chatMessageRepository.save(chatMessage);



        // 2. 읽음 상태 저장
//        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        List<ReadStatus> readStatuses = new ArrayList<>();
        Integer unreadCount = 0;
        for (ChatParticipant c : chatParticipants) {
            boolean isRead = c.getCrewMember().equals(sender); // 보낸 사람은 읽음 처리
            if (!isRead){
                unreadCount++; // 읽지 않은 사람 수
            }
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .crewMember(c.getCrewMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getCrewMember().equals(sender)) // 보낸 사람(또는 입장한 사람)은 읽음 처리
                    .build();
            readStatuses.add(readStatus);
        }
        readStatusRepository.saveAll(readStatuses);

        // 3. Redis 발행을 위한 DTO 생성
        ChatMessageDto responseDto = ChatMessageDto.builder()
                .messageId(chatMessage.getId())
                .roomId(chatRoom.getId())
                .message(chatMessage.getContent())
                .senderEmail(sender.getUser().getEmail())
                .senderNickname(sender.getUser().getNickname())
                .senderProfileImage(sender.getUser().getProfileImageUrl())
                .messageType(chatMessage.getMessageType())
                .delYn("N")
                .editedYn("N")
                .imageUrls(imgUrls)
                .unreadCount(unreadCount)
                .build();

        // 4. Redis 발행
        publishToRedis(responseDto);
    }

    private void publishToRedis(ChatMessageDto dto) {
        try {
            String messageJson = objectMapper.writeValueAsString(dto);
            redisPubSubService.publish("chat", messageJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}