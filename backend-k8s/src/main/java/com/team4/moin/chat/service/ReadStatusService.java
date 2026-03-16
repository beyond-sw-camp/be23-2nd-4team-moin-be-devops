package com.team4.moin.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.moin.chat.domain.ChatParticipant;
import com.team4.moin.chat.domain.ChatRoom;
import com.team4.moin.chat.dtos.ChatReadAckDto;
import com.team4.moin.chat.repository.ReadStatusRepository;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ReadStatusService(ReadStatusRepository readStatusRepository, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.readStatusRepository = readStatusRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    //    읽은 사람 수 추가
    public void markRoomMessagesAsRead(ChatRoom chatRoom, CrewMember crewMember) {
        System.out.println("===== 읽음 처리 시작 =====");
        System.out.println("ChatRoom ID: " + chatRoom.getId());
        System.out.println("CrewMember ID: " + crewMember.getId());

        int updatedCount = readStatusRepository.bulkUpdateReadStatusNative(chatRoom.getId(), crewMember.getId());
        System.out.println("읽음 처리된 메시지 수: " + updatedCount);

//        실시간 읽음 추가
        try{
            ChatReadAckDto ackDto = ChatReadAckDto.builder()
                    .roomId(chatRoom.getId())
                    .build();
            String json = objectMapper.writeValueAsString(ackDto);
            System.out.println("chat-read publish: " + json); ///
            stringRedisTemplate.convertAndSend("chat-read", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
//    오버로드
    public void markRoomMessagesAsRead(ChatParticipant chatParticipant){
        markRoomMessagesAsRead(chatParticipant.getChatRoom(), chatParticipant.getCrewMember());
    }

}
