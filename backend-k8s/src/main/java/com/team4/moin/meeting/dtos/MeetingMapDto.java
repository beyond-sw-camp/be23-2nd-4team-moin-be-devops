package com.team4.moin.meeting.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingMapDto {
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
