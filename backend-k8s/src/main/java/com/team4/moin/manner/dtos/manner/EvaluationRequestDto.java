package com.team4.moin.manner.dtos.manner;

import com.team4.moin.user.domain.enums.GoodBad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationRequestDto {
    private Long crewId;
    private Long meetingId;
    private Long targetUserId;
    private GoodBad evaluation;

}
