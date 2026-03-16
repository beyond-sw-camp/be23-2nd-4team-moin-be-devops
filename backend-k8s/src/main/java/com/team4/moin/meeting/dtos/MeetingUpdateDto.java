package com.team4.moin.meeting.dtos;

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
public class MeetingUpdateDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long meetingId;
    private String meetingName;
    private String profileImage;
    private String place;
    private String description;
    private LocalDateTime meetingAt;
    private Integer maxMembers;
    private Long fee;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private Double latitude;
    private Double longitude;
}
