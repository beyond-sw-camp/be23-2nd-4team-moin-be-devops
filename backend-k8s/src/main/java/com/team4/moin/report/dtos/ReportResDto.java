package com.team4.moin.report.dtos;

import com.team4.moin.report.domain.Report;
import com.team4.moin.report.domain.ReportStatus;
import com.team4.moin.report.domain.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportResDto {
    private Long id;
//    private Long userId;
//    private ReportTargetType targetType;
//    private Long targetId;
//    private ReportStatus status;

    public static ReportResDto fromEntity(Report report){
        return ReportResDto.builder()
                .id(report.getId())
//                .userId(report.getUser().getId())
//                .targetType(report.getTargetType())
//                .targetId(report.getTargetId())
//                .status(report.getStatus())
                .build();
    }



}
