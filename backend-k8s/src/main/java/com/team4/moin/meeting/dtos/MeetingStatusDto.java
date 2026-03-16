package com.team4.moin.meeting.dtos;

import com.team4.moin.meeting.domain.enums.MeetingRecruitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingStatusDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long meetingId;
    @NotNull
    private MeetingRecruitStatus status;
}
