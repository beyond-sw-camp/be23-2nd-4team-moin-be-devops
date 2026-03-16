package com.team4.moin.fee.domain;

import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class RefundLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting; // 어떤 모임에서 발생한 환불인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_member_id", nullable = false)
    private MeetingMember meetingMember; // 누가 환불받았는지

    @Column(nullable = false)
    private Long amount; // 환불된 금액

    @Column(nullable = false)
    private String impUid; // 포트원 결제 고유 번호 (추적용)

    private String refundReason; // 환불 사유 (직접 입력 혹은 선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundType refundType; // 자동 환불(정산 전) vs 승인 환불(정산 후)


}
