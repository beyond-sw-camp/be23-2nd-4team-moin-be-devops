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
public class MeetingListDto {
    private Long crewId;
    private Long meetingId;
    private String meetingName;
    private String place;
    private Long fee;
    private String profileImage;
    private Integer currentMemberCount;
    private Integer maxMembers;
    private LocalDateTime meetingAt;
    private MeetingRecruitStatus recruitStatus;

    public static MeetingListDto fromEntity(Meeting meeting){
        return MeetingListDto.builder()
                .crewId(meeting.getCrew().getId())
                .meetingId(meeting.getId())
                .meetingName(meeting.getName())
                .place(meeting.getPlace())
                .fee(meeting.getFee())
                .meetingAt(meeting.getMeetingAt())
                .currentMemberCount(meeting.getCurrentMemberCount())
                .maxMembers(meeting.getMaxMembers())
                .recruitStatus(meeting.getRecruitStatus())
                .profileImage(meeting.getProfileImage())
                .build();
    }
}
