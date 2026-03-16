package com.team4.moin.chat.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class ReadStatus extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crewMember_id", nullable = false)
    private CrewMember crewMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(nullable = false)
    private Boolean isRead;

    public void updateIsRead(boolean isRead){
        this.isRead = isRead;
    }

}

