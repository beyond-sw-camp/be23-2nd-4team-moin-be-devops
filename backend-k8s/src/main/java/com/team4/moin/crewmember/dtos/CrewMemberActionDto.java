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
public class CrewMemberActionDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long joinId;
}
