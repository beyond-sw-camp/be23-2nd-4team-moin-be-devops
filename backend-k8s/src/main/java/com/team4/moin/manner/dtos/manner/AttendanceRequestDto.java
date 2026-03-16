package com.team4.moin.manner.dtos.manner;

import com.team4.moin.user.domain.enums.EvaluationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRequestDto {
    private Long crewId;
    private Long targetUserId;
    private Long meetingId;
    private EvaluationType type;
}
