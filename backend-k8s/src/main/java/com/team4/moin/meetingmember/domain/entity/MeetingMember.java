package com.team4.moin.meetingmember.domain.entity;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class MeetingMember extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Meeting meeting;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private CrewMember crewMember;
    @Enumerated(EnumType.STRING)
    private MeetingRole role;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingMemberStatus status = MeetingMemberStatus.PENDING;
    private String joinMessage;
    @Column(unique = true)
    private String merchantUid; // 우리 서버가 생성한 고유 주문번호

    private String impUid;      // 포트원 결제 고유번호
    private String refundReason;

    public void updateRole(MeetingRole role) {
        this.role = role;
    }
    public void updateStatus(MeetingMemberStatus status) {
        this.status = status;
    }

    public void completePayment(String impUid) {
        this.impUid = impUid;
        this.updateStatus(MeetingMemberStatus.APPROVED);
        this.updateRole(MeetingRole.MEMBER);
    }
    public boolean canCancelDirectly(boolean isSettled) {
        // 정산 전(isSettled=false)이고, 현재 승인 상태라면 즉시 취소 가능
        return !isSettled && this.status == MeetingMemberStatus.APPROVED;
    }
    public void updateRefundReason(String reason) {
        this.refundReason = reason;
    }
}




