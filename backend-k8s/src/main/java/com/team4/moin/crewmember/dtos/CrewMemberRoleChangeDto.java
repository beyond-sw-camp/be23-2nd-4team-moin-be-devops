package com.team4.moin.crewmember.dtos;

import com.team4.moin.crew.domain.enums.CrewRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMemberRoleChangeDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long crewMemberId;
    @NotNull
    private CrewRole role;
}
