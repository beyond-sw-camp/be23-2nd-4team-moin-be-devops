package com.team4.moin.admin.dtos;

import com.team4.moin.feed.domain.Comment;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.report.domain.ReportTargetType;
import com.team4.moin.user.domain.entitys.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportSummaryResponseDto {
    private Long reportId;
    private String reporterEmail;      // 신고한 사람 email
    private String reportedEmail;      // 신고 당한 사람 email
    private String reportContent;      // 신고 내용
    private ReportTargetType targetType; // 신고 유형

    // USER 신고
    public static ReportSummaryResponseDto fromUser(Long reportId, User reporter, User reported, String content) {
        return ReportSummaryResponseDto.builder()
                .reportId(reportId)
                .reporterEmail(reporter.getEmail())
                .reportedEmail(reported.getEmail())
                .reportContent(content)
                .targetType(ReportTargetType.USER)
                .build();
    }

    // MEETING 신고
    public static ReportSummaryResponseDto fromMeeting(Long reportId, User reporter, Meeting meeting, String content) {
        return ReportSummaryResponseDto.builder()
                .reportId(reportId)
                .reporterEmail(reporter.getEmail())
                .reportedEmail("[모임] " + meeting.getName())
                .reportContent(content)
                .targetType(ReportTargetType.MEETING)
                .build();
    }

    // NOSHOW 신고
    public static ReportSummaryResponseDto fromNoShow(Long reportId, User reporter, User reported, String content) {
        return ReportSummaryResponseDto.builder()
                .reportId(reportId)
                .reporterEmail(reporter.getEmail())
                .reportedEmail(reported.getEmail())
                .reportContent(content)
                .targetType(ReportTargetType.NOSHOW)
                .build();
    }

    // COMMENT 신고
    public static ReportSummaryResponseDto fromComment(Long reportId, User reporter, Comment comment, String content) {
        return ReportSummaryResponseDto.builder()
                .reportId(reportId)
                .reporterEmail(reporter.getEmail())
                .reportedEmail(comment.getUser().getEmail())
                .reportContent(content)
                .targetType(ReportTargetType.COMMENT)
                .build();
    }
}
