package com.team4.moin.meeting.dtos;

import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingRecruitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingDetailDto {
    private Long meetingId;
    private Long crewId;
    private String meetingName;
    private String place;
    private String description;
    private String profileImage;
    private LocalDateTime meetingAt;
    private Integer currentMemberCount;
    private Integer maxMembers;
    private MeetingRecruitStatus recruitStatus;
    private Long fee;
    //지도좌표
    private Double latitude;
    private Double longitude;
    public static MeetingDetailDto fromEntity(Meeting meeting){
        return MeetingDetailDto.builder()
                .meetingId(meeting.getId())
                .crewId(meeting.getCrew().getId())
                .meetingName(meeting.getName())
                .place(meeting.getPlace())
                .description(meeting.getDescription())
                .profileImage(meeting.getProfileImage())
                .meetingAt(meeting.getMeetingAt())
                .currentMemberCount(meeting.getCurrentMemberCount())
                .maxMembers(meeting.getMaxMembers())
                .recruitStatus(meeting.getRecruitStatus())
                .fee(meeting.getFee())
                .latitude(meeting.getLatitude())
                .longitude(meeting.getLongitude())
                .build();
    }
}
