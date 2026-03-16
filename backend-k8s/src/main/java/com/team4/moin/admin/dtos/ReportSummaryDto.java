package com.team4.moin.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportSummaryDto {
    private Long targetId;         // 신고 대상 유저의 id
    private String nickname; // 대상 닉네임
    private String email;    // 대상 이메일
    private Long pendingCount;     // 현재 대기 중인 신고 수
}
