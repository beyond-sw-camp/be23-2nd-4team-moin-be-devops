package com.team4.moin.user.domain.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GoodBad {
    GOOD,     // 상호 평가: 좋아요 (+0.2)
    BAD,      // 상호 평가: 싫어요 (-0.3)
}
