package com.team4.moin.report.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

import java.lang.reflect.Member;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
public class Report extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 신고 대상 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;
    // USER, MEETING, COMMENT, NOSHOW

    // 신고 대상 ID
    @Column(nullable = false)
    private Long targetId;

    // 신고 사유 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReasonType reasonType;

    // 신고 상세 사유
    @Column(length = 500)
    private String reasonText;

    // 신고 상태
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    public void changeStatus(ReportStatus status) {
        this.status = status;
    }

}
