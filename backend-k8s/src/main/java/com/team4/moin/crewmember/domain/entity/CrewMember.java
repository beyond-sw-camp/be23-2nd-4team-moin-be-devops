package com.team4.moin.crewmember.domain.entity;

import com.team4.moin.chat.domain.ChatRoom;
import com.team4.moin.common.domain.BaseTimeEntity;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class CrewMember extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Crew crew;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private CrewMemberStatus status = CrewMemberStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CrewRole role = CrewRole.MEMBER;
    private String joinMessage;
    @Column(precision = 2, scale = 1) // 0.0 ~ 9.9
    private BigDecimal crewRating;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = true) //
    private ChatRoom chatRoom;

    public void updateStatus(CrewMemberStatus status) {
        this.status = status;
    }
    public void updateRole(CrewRole role) {
        this.role = role;
    }
    public void updateJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }
    public void updateCrewRating(BigDecimal score) {
        this.crewRating = score;
    }

//    채팅방 관련
    public void assignChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void clearChatRoom() {
        this.chatRoom = null;
    }

}
