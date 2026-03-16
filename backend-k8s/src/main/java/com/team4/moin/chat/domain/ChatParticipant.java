package com.team4.moin.chat.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
// 채팅방 참여자
public class ChatParticipant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ChatParticipantStatus status = ChatParticipantStatus.JOINED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crewMember_id", nullable = false)
    private CrewMember crewMember;

    private Long lastReadChatId; // 해당 참여자가 마지막으로 읽은 messageId

    private Long startMessageId; // 입장 시점의 messageId 입장 이후 메시지만 조회 가능

    public void leave(){
        this.status = ChatParticipantStatus.LEAVED;
        this.lastReadChatId = null;
        this.startMessageId = null;
    }
    
//    재가입 시, 상태 변경
    public void rejoin(Long startMessageId){
        this.status = ChatParticipantStatus.JOINED;
        this.startMessageId = startMessageId;
    }

//    1:1 채팅방 나가기
    public void softLeave(){
        this.status = ChatParticipantStatus.HIDDEN;
//        this.lastReadChatId = null;
        this.startMessageId = null;
    }

    // 1:1 HIDDEN → JOINED 자동 복귀용
    public void restoreFromHidden(Long startMessageId){
        if (this.status != ChatParticipantStatus.HIDDEN) {
            throw new IllegalStateException("HIDDEN 상태가 아닙니다.");
        }
        this.status = ChatParticipantStatus.JOINED;
        this.startMessageId = startMessageId; // 복귀 시점 이후 메시지부터
    }

    public void updateStartMessageId(Long startMessageId){
        this.startMessageId = startMessageId;
    }
}

