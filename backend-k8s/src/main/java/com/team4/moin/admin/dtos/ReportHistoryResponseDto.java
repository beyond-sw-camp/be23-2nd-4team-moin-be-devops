package com.team4.moin.admin.dtos;

import com.team4.moin.report.domain.Report;
import com.team4.moin.report.domain.ReportReasonType;
import com.team4.moin.report.domain.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportHistoryResponseDto {
    private Long reportId;
    private String reportedEmail;    // 닉네임 → 이메일로 변경
    private String reasonText;       // 신고 사유
    private ReportTargetType targetType; // 처리 유형
    private Long reportedCount;      // 신고 수

    public static ReportHistoryResponseDto fromEntity(Report report, String email, Long totalCount) {
        return ReportHistoryResponseDto.builder()
                .reportId(report.getId())
                .reportedEmail(email)          // ✅ 이메일
                .reasonText(report.getReasonText())
                .targetType(report.getTargetType())
                .reportedCount(totalCount)
                .build();
    }
}
