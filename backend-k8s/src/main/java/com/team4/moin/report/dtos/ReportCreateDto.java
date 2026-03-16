package com.team4.moin.report.dtos;

import com.team4.moin.report.domain.Report;
import com.team4.moin.report.domain.ReportReasonType;
import com.team4.moin.report.domain.ReportStatus;
import com.team4.moin.report.domain.ReportTargetType;
import com.team4.moin.user.domain.entitys.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportCreateDto {
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReasonType reasonType;
    private String reasonText;

    public Report toEntity(User user){
        return Report.builder()
                .user(user)
                .targetType(this.targetType)
                .targetId(this.targetId)
                .reasonType(this.reasonType)
                .reasonText(this.reasonText)
                .build();
    }

}

