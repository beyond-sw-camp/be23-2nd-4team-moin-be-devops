package com.team4.moin.meeting.dtos;

import com.team4.moin.meeting.domain.entitys.Meeting;
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
public class MyMeetingListDto {
    private Long meetingId;
    private String profileImage;
    private String meetingName;
    private String crewName;
    private String place;
    private Long fee;
    private LocalDateTime meetingAt;
    private MeetingRecruitStatus recruitStatus;

    public static MyMeetingListDto fromEntity(Meeting meeting) {
        return MyMeetingListDto.builder()
                .meetingId(meeting.getId())
                .meetingName(meeting.getName())
                .profileImage(meeting.getProfileImage())
                .crewName(meeting.getCrew().getName())
                .place(meeting.getPlace())
                .fee(meeting.getFee())
                .meetingAt(meeting.getMeetingAt())
                .recruitStatus(meeting.getRecruitStatus())
                .build();
    }
}
