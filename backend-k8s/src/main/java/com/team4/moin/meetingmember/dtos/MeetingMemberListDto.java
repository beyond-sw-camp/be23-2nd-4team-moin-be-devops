package com.team4.moin.meetingmember.dtos;

import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingMemberListDto {
    private Long meetingMemberId;
    private Long userId;
    private String userEmail;
    private String nickname;
    private String profileImageUrl;
    private BigDecimal mannerScore;
    private MeetingRole role;
    private Long crewMemberId;
    private Long crewId;

    public static MeetingMemberListDto fromEntity(MeetingMember meetingMember){
        return MeetingMemberListDto.builder()
                .meetingMemberId(meetingMember.getId())
                .userId(meetingMember.getCrewMember().getUser().getId())
                .userEmail(meetingMember.getCrewMember().getUser().getEmail())
                .nickname(meetingMember.getCrewMember().getUser().getNickname())
                .profileImageUrl(meetingMember.getCrewMember().getUser().getProfileImageUrl())
                .mannerScore(meetingMember.getCrewMember().getUser().getMannerScore())
                .role(meetingMember.getRole())
                .crewMemberId(meetingMember.getCrewMember().getId())
                .crewId(meetingMember.getCrewMember().getCrew().getId())
                .build();
    }
}
