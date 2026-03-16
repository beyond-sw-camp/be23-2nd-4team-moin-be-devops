package com.team4.moin.crewmember.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMemberLeftDto {
    @NotNull
    private Long crewId;
    private String leftReason; // 탈퇴사유 -있으면 받고 없으면 null
    private Long nextOwnerCrewMemberId;//크루장 탈퇴시 필수

}
