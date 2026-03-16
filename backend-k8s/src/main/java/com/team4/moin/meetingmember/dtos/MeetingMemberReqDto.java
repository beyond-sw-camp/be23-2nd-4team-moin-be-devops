package com.team4.moin.meetingmember.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingMemberReqDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long meetingId;
    private String joinMessage; //가입인사
}


