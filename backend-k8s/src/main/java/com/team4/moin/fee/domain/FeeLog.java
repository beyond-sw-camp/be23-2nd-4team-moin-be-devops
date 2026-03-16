package com.team4.moin.fee.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class FeeLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meetingId; // 어떤 모임의 수입인지

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    private FeeStatus status;
}
