package com.team4.moin.user.domain.enums;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@AllArgsConstructor
public enum EvaluationType {

    ATTEND,   // 행동 기록: 모임 참석 (+0.2)
    NO_SHOW   // 행동 기록: 노쇼 (-0.5)
}
