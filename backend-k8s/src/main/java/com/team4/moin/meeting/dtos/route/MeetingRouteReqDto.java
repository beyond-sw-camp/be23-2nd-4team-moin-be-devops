package com.team4.moin.meeting.dtos.route;

import com.team4.moin.meeting.domain.enums.RouteMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingRouteReqDto {
    private Long crewId;
    private Long meetingId;
    private RouteMode mode;
//    현재 내위치
    private double startLat;
    private double startLng;
}
