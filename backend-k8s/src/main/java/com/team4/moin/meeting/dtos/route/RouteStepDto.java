package com.team4.moin.meeting.dtos.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteStepDto {
    private String mode;     //  BUS / SUBWAY
    private String route;    // 버스번호 나 지하철 호선
    private String fromName; // 출발 정류장/역
    private String toName;   // 도착 정류장/역
    private Integer distance;
    private Integer time;
    private Integer stations; // 정거장 수
}
