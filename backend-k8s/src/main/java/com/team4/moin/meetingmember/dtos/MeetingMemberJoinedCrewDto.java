package com.team4.moin.meetingmember.dtos;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crewmember.dtos.CrewMemberJoinedCrewDto;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.user.domain.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingMemberJoinedCrewDto {

    private Long meetingId;
    private String name;
    private String profileImage;
    private CategoryType categoryType;
    private String region;
    private String district;
    private Integer currentMemberCount;

    public static CrewMemberJoinedCrewDto fromEntity(Meeting meeting, Crew crew) {
        return CrewMemberJoinedCrewDto.builder()
                .crewId(meeting.getId())
                .crewName(meeting.getName())
                .crewImage(meeting.getProfileImage())
                .categoryType(crew.getCategoryType())
                .region(crew.getRegion())
                .district(crew.getDistrict())
                .currentMemberCount(crew.getCurrentMemberCount())
                .build();
    }
    
}
