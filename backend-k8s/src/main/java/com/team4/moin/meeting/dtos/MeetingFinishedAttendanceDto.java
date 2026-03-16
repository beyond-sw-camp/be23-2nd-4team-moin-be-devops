package com.team4.moin.meeting.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingFinishedAttendanceDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long meetingId;

    // 노쇼 처리할 meetingMemberId 목록
    private List<Long> noShowMeetingMemberIds;
}
