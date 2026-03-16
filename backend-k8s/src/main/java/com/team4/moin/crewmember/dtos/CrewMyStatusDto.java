package com.team4.moin.crewmember.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMyStatusDto {
    private String CrewMemberStatus; // NONE | PENDING | APPROVED
    private String role; // OWNER | MANAGER | MEMBER | null
}
