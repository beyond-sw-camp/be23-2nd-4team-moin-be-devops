package com.team4.moin.crewmember.dtos;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.user.domain.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMemberJoinedCrewDto {
    private Long crewId;
    private String crewName;
    private String crewImage;
    private CategoryType categoryType;
    private String region;
    private String district;
    private Integer currentMemberCount;

    public static CrewMemberJoinedCrewDto fromEntity(Crew crew) {
        return CrewMemberJoinedCrewDto.builder()
                .crewId(crew.getId())
                .crewName(crew.getName())
                .crewImage(crew.getCrewImage())
                .categoryType(crew.getCategoryType())
                .region(crew.getRegion())
                .district(crew.getDistrict())
                .currentMemberCount(crew.getCurrentMemberCount())
                .build();
    }
}
