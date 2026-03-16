package com.team4.moin.chat.service;

import com.team4.moin.chat.domain.ChatParticipant;
import com.team4.moin.chat.domain.ChatParticipantStatus;
import com.team4.moin.chat.repository.ChatParticipantRepository;
import com.team4.moin.chat.repository.ChatRoomRepository;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatValidatorService {
    private final ChatRoomRepository chatRoomRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public ChatValidatorService(ChatRoomRepository chatRoomRepository, CrewMemberRepository crewMemberRepository, ChatParticipantRepository chatParticipantRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.chatParticipantRepository = chatParticipantRepository;
    }

    public ChatParticipant validateAndGetParticipant(String email, Long roomId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found"));

        ChatParticipant participant = chatParticipantRepository.findByRoomIdAndUserEmail(roomId, email)
                .orElseThrow(() -> new IllegalArgumentException("해당 room에 권한이 없습니다."));

        if (participant.getStatus() != ChatParticipantStatus.JOINED) {
            throw new IllegalArgumentException("해당 room에 권한이 없습니다.");
        }

        return participant; // 조회한 객체 반환
    }
}
