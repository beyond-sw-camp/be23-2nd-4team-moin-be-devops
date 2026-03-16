package com.team4.moin.fee.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundApprovalDto {

    private boolean approved; // 승인 여부 (true: 승인, false: 거절)
}
