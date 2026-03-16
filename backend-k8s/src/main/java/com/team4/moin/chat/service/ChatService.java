package com.team4.moin.chat.service;

import com.team4.moin.chat.domain.*;
import com.team4.moin.chat.dtos.ChatRoomSearchDto;
import com.team4.moin.chat.dtos.MyChatListResDto;
import com.team4.moin.chat.dtos.PrivateRoomResDto;
import com.team4.moin.chat.repository.ChatMessageRepository;
import com.team4.moin.chat.repository.ChatParticipantRepository;
import com.team4.moin.chat.repository.ChatRoomRepository;
import com.team4.moin.chat.repository.ReadStatusRepository;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChatMessageService chatMessageService;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusService readStatusService;
    private final CrewRepository crewRepository;

    @Autowired
    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, CrewMemberRepository crewMemberRepository, ReadStatusRepository readStatusRepository, UserRepository userRepository, ChatMessageService chatMessageService, ChatMessageRepository chatMessageRepository, ReadStatusService readStatusService, CrewRepository crewRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
        this.chatMessageService = chatMessageService;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusService = readStatusService;
        this.crewRepository = crewRepository;
    }

    //    그룹채팅방 생성
    public void createGroupRoom(String roomName, Crew crew){
        User user = userRepository.findAllByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member cannot be found"));
        CrewMember crewMember = crewMemberRepository.findByCrewAndUser(crew, user).orElseThrow(() -> new EntityNotFoundException("크루 멤버 없음"));
//        채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomImage(crew.getCrewImage())
                .name(roomName)
                .isGroupChat("Y")
                .crew(crew)
                .build();
        chatRoomRepository.save(chatRoom);

        addParticipantToRoom(chatRoom, crewMember);
    }

    //    그룹채팅 참여자 추가
    public void addParticipantToGroupChat(ChatRoom chatRoom, CrewMember crewMember) {
//        1대1 채팅은 추가 참여 불가
        if (chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("그룹채팅이 아닙니다");
        }

//        이미 참여자인지 검증
        Optional<ChatParticipant> optParticipant = chatParticipantRepository.findByChatRoomAndCrewMember(chatRoom, crewMember);
        if (optParticipant.isPresent()) {
            ChatParticipant participant = optParticipant.get();
            if (participant.getStatus() == ChatParticipantStatus.LEAVED){
                // 재가입 시 입장 시점의 최신 messageId 기록
                Long latestMessageId = chatMessageService.getLatestMessageId(chatRoom);
                participant.rejoin(latestMessageId);
                crewMember.assignChatRoom(chatRoom);
//                재입장 시, 메시지 다시 전송
                chatMessageService.sendEnterMessage(chatRoom, crewMember);
            }
        } else {
            addParticipantToRoom(chatRoom, crewMember);
        }
    }

    //         ChatParticipant 객체 생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, CrewMember crewMember){
        Long latestMessageId = chatMessageService.getLatestMessageId(chatRoom);
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .crewMember(crewMember)
                .startMessageId(latestMessageId)
                .build();
        chatParticipantRepository.save(chatParticipant);
//        입장 메시지
        chatMessageService.sendEnterMessage(chatRoom, crewMember);
    }


    //    1:1 채팅방 나가기(크루 탈퇴 또는 회원 탈퇴
    public void softLeaveChatRoom(Long roomId){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ChatParticipant participant = chatParticipantRepository.findByRoomIdAndUserEmail(roomId, email)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방의 참여자 정보를 찾을 수 없습니다."));

        // 그룹 채팅방이면 나가기 불가
        if (participant.getChatRoom().getIsGroupChat().equals("Y")) {
            throw new IllegalArgumentException("그룹 채팅방은 크루 탈퇴 시에만 나갈 수 있습니다.");
        }

        if (participant.getStatus() == ChatParticipantStatus.LEAVED || participant.getStatus() == ChatParticipantStatus.HIDDEN) {
            throw new IllegalStateException("이미 채팅방을 나간 상태입니다.");
        }
        participant.softLeave();

        // 전원 softLeave면 방 삭제x -> 둘 다 leaved인 경우 방 삭제
        ChatRoom chatRoom = participant.getChatRoom();
        List<ChatParticipant> remaining = chatParticipantRepository.findByChatRoomAndStatus(chatRoom, ChatParticipantStatus.LEAVED);
        if (remaining.size() == 2) {
            chatRoom.deleteChatRoom();
        }
    }

    public void leaveAllChatRoomsForCrewMember(CrewMember crewMember){

        List<ChatParticipant> allParticipants = chatParticipantRepository.findAllByCrewMember(crewMember);
        for (ChatParticipant chatParticipant : allParticipants){
            if (chatParticipant.getStatus() == ChatParticipantStatus.JOINED) {
                chatParticipant.leave();
                // 3. 방 삭제
                ChatRoom chatRoom = chatParticipant.getChatRoom();
                List<ChatParticipant> remaining = chatParticipantRepository.findByChatRoomAndStatus(chatRoom, ChatParticipantStatus.JOINED);
                if (remaining.isEmpty()) {
                    chatRoom.deleteChatRoom();
                }
            }
        }
//        해당 CrewMember의 ReadStatus 전체 삭제
        readStatusRepository.deleteAllByCrewMember(crewMember);
        // CrewMember chatRoom 참조 null 처리
        crewMember.clearChatRoom();
    }


    public void messageRead(Long roomId){
//        a라는 사람이 해당 채팅방의 채팅을 모두 읽음 처리.
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("없는 방입니다."));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // roomId + email로 participant를 바로 조회 → CrewMember 중복 문제 해결
        ChatParticipant participant = chatParticipantRepository
                .findByRoomIdAndUserEmail(roomId, email)
                .orElseThrow(() -> new EntityNotFoundException("참여자가 아닙니다."));

        if (participant.getStatus() != ChatParticipantStatus.JOINED) {
            throw new IllegalStateException("퇴장한 사용자입니다.");
        }

        readStatusService.markRoomMessagesAsRead(participant);
    }

    //    현재 로그인한 사용자의 CrewMember 조회1
    private List<CrewMember> getCurrentCrewMembers() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return crewMemberRepository.findAllByUserEmail(email); // List 반환 레포지토리 메서드 필요
    }

    //    채팅방 이름 검색3
    private List<ChatRoom> getSearchedRooms(ChatRoomSearchDto searchDto){
        Specification<ChatRoom> specification = new Specification<ChatRoom>() {
            @Override
            public Predicate toPredicate(Root<ChatRoom> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();

                if (searchDto.getRoomName() != null && !searchDto.getRoomName().isEmpty()){
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getRoomName() + "%"));
                }

                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateArr.length; i++){
                    predicateArr[i] = predicateList.get(i);
                }
                return criteriaBuilder.and(predicateArr);
            }
        };
        return chatRoomRepository.findAll(specification);
    }
    //    검색된 채팅방 기준으로 ChatParticipant 필터링4
    private List<ChatParticipant> filterBySearchDto(List<ChatParticipant> participants, ChatRoomSearchDto searchDto) {
        if (searchDto == null || searchDto.getRoomName() == null || searchDto.getRoomName().isEmpty()) {
            return participants;
        }

        List<ChatRoom> searchedRooms = getSearchedRooms(searchDto);

        List<ChatParticipant> filtered = new ArrayList<>();
        for (ChatParticipant cp : participants) {
            if (searchedRooms.contains(cp.getChatRoom())) {
                filtered.add(cp);
            }
        }
        return filtered;
    }

    //    채팅방 목록 공통 메서드2
    private List<MyChatListResDto> buildChatListResDtoList(List<ChatParticipant> participants, List<CrewMember> crewMembers){
        if (participants.isEmpty()) return new ArrayList<>();

        List<Long> chatRoomIds = participants.stream().map(cp -> cp.getChatRoom().getId()).toList();

        // 안 읽은 메시지 수
        List<Object[]> unreadCounts = readStatusRepository.countUnreadByRoomsAndCrewMembers(chatRoomIds, crewMembers);
        Map<Long, Long> unreadCountMap = new HashMap<>();
        for (Object[] row : unreadCounts) {
            unreadCountMap.put((Long) row[0], (Long) row[1]);
        }

        // 채팅방별 인원 수
        List<Object[]> memberCounts = chatParticipantRepository.countMembersByChatRoomIds(chatRoomIds);
        Map<Long, Integer> memberCountMap = new HashMap<>();
        for (Object[] row : memberCounts) {
            memberCountMap.put((Long) row[0], ((Long) row[1]).intValue());
        }

        // 채팅방별 최신 메시지 시간 + 내용
        List<Object[]> lastMessages = chatMessageRepository.findLastMessageByChatRoomIds(chatRoomIds);
        Map<Long, LocalDateTime> lastMessageTimeMap = new HashMap<>();
        Map<Long, String> lastMessageContentMap = new HashMap<>();
        for (Object[] row : lastMessages) {
            Long roomId = ((Number) row[0]).longValue();
            lastMessageContentMap.put(roomId, (String) row[1]);
            lastMessageTimeMap.put(roomId, ((Timestamp) row[2]).toLocalDateTime());
        }

        List<MyChatListResDto> result = new ArrayList<>();
        for (ChatParticipant cp : participants) {
            Long roomId = cp.getChatRoom().getId();
            boolean isGroup = "Y".equals(cp.getChatRoom().getIsGroupChat());

            // 메시지가 없는 방은 목록에서 제외
            if (!isGroup && lastMessageTimeMap.get(roomId) == null) continue;

            String[] opponentInfo = {null, null};
            if (!isGroup){
//                해당 방의 참여자 중 나를 제외한 상대방 조회
                chatParticipantRepository.findOpponentInPrivateRoom(cp.getChatRoom(), cp.getCrewMember())
                        .ifPresent(opponent -> {
                            if (opponent.getStatus() == ChatParticipantStatus.LEAVED) {
                                opponentInfo[0] = "알 수 없음";
                                opponentInfo[1] = null;
                            } else {
                                opponentInfo[0] = opponent.getCrewMember().getUser().getNickname();
                                opponentInfo[1] = opponent.getCrewMember().getUser().getProfileImageUrl();
                            }
                        });
            }

            String privateRoomName = null;
            if (!isGroup) {
                String crewName = cp.getChatRoom().getCrew() != null
                        ? cp.getChatRoom().getCrew().getName()
                        : "";
                privateRoomName = crewName + "-" + (opponentInfo[0] != null ? opponentInfo[0] : "알 수 없음");
            }


            MyChatListResDto dto = MyChatListResDto.builder()
                    .roomId(roomId)
                    .roomName(isGroup ? cp.getChatRoom().getName() : privateRoomName)
                    .chatRoomImage(isGroup ? cp.getChatRoom().getChatRoomImage() : opponentInfo[1])
                    .isGroupChat(cp.getChatRoom().getIsGroupChat())
                    .unReadCount(unreadCountMap.getOrDefault(roomId, 0L))
                    .memberCount(isGroup ? memberCountMap.getOrDefault(roomId, 0) : null)
                    .lastMessageTime(lastMessageTimeMap.getOrDefault(roomId, null))
                    .lastMessageContent(lastMessageContentMap.getOrDefault(roomId, null))
                    .build();
            result.add(dto);
        }
        return result;
    }

    //    0. 내 전체 채팅방 조회
    public List<MyChatListResDto> getAllMyChatRooms(ChatRoomSearchDto searchDto) {
        List<CrewMember> crewMembers = getCurrentCrewMembers();

        List<ChatParticipant> allParticipants = new ArrayList<>();
        for (CrewMember cm : crewMembers) {
            List<ChatParticipant> joined = chatParticipantRepository
                    .findAllByCrewMemberAndStatusWithChatRoom(cm, ChatParticipantStatus.JOINED);
            allParticipants.addAll(joined);
        }
        return buildChatListResDtoList(filterBySearchDto(allParticipants, searchDto), crewMembers);
    }


    //    1. 내 그룹 채팅방 전체 조회
    public List<MyChatListResDto> getMyGroupChatRooms(ChatRoomSearchDto searchDto){
        List<CrewMember> crewMemberList = getCurrentCrewMembers();

        List<ChatParticipant> groupParticipants = new ArrayList<>();
        for (CrewMember c : crewMemberList){
            //            내가 가입되어 있는 채팅방인지
            List<ChatParticipant> joined = chatParticipantRepository
                    .findAllByCrewMemberAndStatusWithChatRoom(c, ChatParticipantStatus.JOINED);
            for (ChatParticipant cp : joined){
                if (cp.getChatRoom().getIsGroupChat().equals("Y")){
                    groupParticipants.add(cp);
                }
            }
        }
        return buildChatListResDtoList(filterBySearchDto(groupParticipants, searchDto), crewMemberList);
    }
    //    2. 내 1:1 채팅방 전체 조회
    public List<MyChatListResDto> getMyChatRooms(ChatRoomSearchDto searchDto) {
        List<CrewMember> crewMembers = getCurrentCrewMembers();

        List<ChatParticipant> privateParticipants = new ArrayList<>();
        for (CrewMember cm : crewMembers) {
            List<ChatParticipant> joined = chatParticipantRepository
                    .findAllByCrewMemberAndStatusWithChatRoom(cm, ChatParticipantStatus.JOINED);
            for (ChatParticipant cp : joined) {
                if (cp.getChatRoom().getIsGroupChat().equals("N")) {
                    privateParticipants.add(cp);
                }
            }
        }
        return buildChatListResDtoList(filterBySearchDto(privateParticipants, searchDto), crewMembers);
    }

    // 3) 특정 크루의 그룹 채팅방 조회
    public List<MyChatListResDto> getCrewChatRoom(Long crewId, ChatRoomSearchDto searchDto) {
        List<CrewMember> crewMembers = getCurrentCrewMembers();

        List<ChatParticipant> crewGroupParticipants = new ArrayList<>();
        for (CrewMember cm : crewMembers) {
            if (!cm.getCrew().getId().equals(crewId)) continue; // 해당 크루 아니면 스킵

            List<ChatParticipant> joined = chatParticipantRepository
                    .findAllByCrewMemberAndStatusWithChatRoom(cm, ChatParticipantStatus.JOINED);
            for (ChatParticipant cp : joined) {
                if ("Y".equals(cp.getChatRoom().getIsGroupChat())) {
                    crewGroupParticipants.add(cp);
                }
            }
        }
        return buildChatListResDtoList(filterBySearchDto(crewGroupParticipants, searchDto), crewMembers);
    }

    public PrivateRoomResDto getOrCreatePrivateRoom(Long otherUserId, Long crewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // ✅ User ID + crewId 기준으로 기존 방 먼저 조회
        Optional<ChatRoom> existingRoom = chatParticipantRepository
                .findExistingPrivateRoom(user.getId(), otherUser.getId(), crewId);

        if (existingRoom.isPresent()) {
            ChatRoom chatRoom = existingRoom.get();

            // 내 참여자 HIDDEN 복구
            chatParticipantRepository.findByRoomIdAndUserEmail(chatRoom.getId(), email)
                    .ifPresent(p -> {
                        if (p.getStatus() == ChatParticipantStatus.HIDDEN) {
                            Long latestMessageId = chatMessageService.getLatestMessageId(chatRoom);
                            p.restoreFromHidden(latestMessageId);
                        }
                    });

            // 상대방 참여자 HIDDEN 복구
            chatParticipantRepository.findByRoomIdAndUserEmail(chatRoom.getId(), otherUser.getEmail())
                    .ifPresent(p -> {
                        if (p.getStatus() == ChatParticipantStatus.HIDDEN) {
                            Long latestMessageId = chatMessageService.getLatestMessageId(chatRoom);
                            p.restoreFromHidden(latestMessageId);
                        }
                    });

            String opponentNickname = otherUser.getNickname();
            String crewName = chatRoom.getCrew() != null ? chatRoom.getCrew().getName() : "";

            return PrivateRoomResDto.builder()
                    .roomId(chatRoom.getId())
                    .roomName(crewName + "-" + opponentNickname)
                    .build();
        }

        CrewMember crewMember = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalArgumentException("해당 크루의 멤버가 아닙니다."));

        CrewMember otherCrewMember = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, otherUser.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalArgumentException("상대방이 해당 크루의 멤버가 아닙니다."));

        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")
                .name(crewMember.getCrew().getName() + "-" + crewMember.getUser().getNickname() + "-" + otherCrewMember.getUser().getNickname())
                .crew(crewMember.getCrew())
                .build();
        chatRoomRepository.save(newRoom);
        addParticipantToRoom(newRoom, crewMember);
        addParticipantToRoom(newRoom, otherCrewMember);
        return PrivateRoomResDto.builder()
                .roomId(newRoom.getId())
                .roomName(crewMember.getCrew().getName() + "-" + otherCrewMember.getUser().getNickname())
                .build();
    }

    public CrewRole getMyRoleInRoom(Long roomId ){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return chatParticipantRepository.findRoleByChatRoomIdAndUserEmail(roomId, email)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방에 참여자가 아니거나, 방이 존재하지 않습니다."));
    }

    public void deleteAllChatRoomsByCrew(Long crewId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByCrewId(crewId);
        for (ChatRoom chatRoom : chatRooms) {
            chatRoom.deleteChatRoom(); // soft delete (cascade로 ChatParticipant, ChatMessage 삭제)
        }
    }
}