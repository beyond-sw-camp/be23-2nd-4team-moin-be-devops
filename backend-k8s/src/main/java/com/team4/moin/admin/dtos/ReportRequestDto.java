package com.team4.moin.admin.dtos;

import com.team4.moin.report.domain.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportRequestDto {
    private Long reportId;      // 어떤 신고를 처리할 것인가
}
