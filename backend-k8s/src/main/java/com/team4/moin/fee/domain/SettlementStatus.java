package com.team4.moin.fee.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SettlementStatus {
    PENDING,    // 정산 시도 중 (이체 API 호출 전후 상태)
    COMPLETED,  // 정산 완료 (이체 성공)
    FAILED      // 정산 실패 (이체 에러 발생)
}
