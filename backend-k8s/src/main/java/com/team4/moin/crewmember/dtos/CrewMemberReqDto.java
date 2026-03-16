package com.team4.moin.crewmember.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMemberReqDto {
    @NotNull
    private Long crewId;
    private String joinMessage; //가입인사
}
