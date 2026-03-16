package com.team4.moin.meetingmember.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class MeetingEvaluationRequestDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long meetingId;
    @NotNull
    private Long targetUserId;
    @NotBlank
    private String type; // GOOD , BAD
}
