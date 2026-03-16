package com.team4.moin.meeting.dtos.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingRouteResDto {
    // 지도에 선 그릴 좌표들
    private List<double[]> path;
    // 총 거리
    private Integer distance;
    // 총 소요시간
    private Integer time;
    //  대중교통 상세 구간
    private List<RouteStepDto> steps;
    //  환승횟수
    private Integer transfers;
    //  요금
    private Integer fare;

}
