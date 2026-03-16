package com.team4.moin.fee.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class SettlementLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting; // 어떤 모임에서 나간 돈인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private User user; // 어느 모임장에게 보냈는지

    @Column(nullable = false)
    private Long amount; // 이번에 보낸 금액

    @Column(nullable = false)
    private String bankName; // 보낸 은행 (기록용)

    @Column(nullable = false)
    private String accountNumber; // 보낸 계좌 (기록용)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SettlementStatus isSettled = SettlementStatus.PENDING;

    public void markAsCompleted() {
        this.isSettled = SettlementStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.isSettled = SettlementStatus.FAILED;
    }
}