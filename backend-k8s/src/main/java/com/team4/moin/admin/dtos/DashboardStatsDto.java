package com.team4.moin.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DashboardStatsDto {

    private long totalUsers;
    private long pendingReportCount;
    private long acceptedReportCount;
    private long totalSettlementCount;
}
