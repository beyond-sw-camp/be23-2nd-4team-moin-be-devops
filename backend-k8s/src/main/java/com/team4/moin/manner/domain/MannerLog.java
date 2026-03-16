package com.team4.moin.manner.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class MannerLog extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User evaluator; // 평가자 (출석체크 시 null 가능)

    @ManyToOne(fetch = FetchType.LAZY)
    private User targetUser; // 피평가자

    @ManyToOne(fetch = FetchType.LAZY)
    private Crew crew;

    private String type; // "GOOD", "BAD", "ATTEND", "NO_SHOW" 저장

    private BigDecimal scoreDelta; // 가공되지 않은 숫자 (예: 0.2, -0.5)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting; // 출석/노쇼, 모임평가에 사용
}
