package com.team4.moin.meetingmember.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingJoinResponseDto {
    private Long joinId;
    private boolean approved; // true = 무료 모임 가입 신청시 바로 가입완료
}
