package com.team4.moin.crew.dtos;

import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.user.domain.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyCrewListDto {
    private Long crewId;
    private String crewName;
    private CategoryType categoryType;
    private String region;
    private String district;
    private String crewImage;
    private BigDecimal ratingAvg;
    private Long viewCount;
    private Integer currentMemberCount;
    private Integer maxMembers;
    private CrewRole role;

    public static MyCrewListDto fromEntity(CrewMember crewMember){
        return MyCrewListDto.builder()
                .crewId(crewMember.getCrew().getId())
                .crewName(crewMember.getCrew().getName())
                .categoryType(crewMember.getCrew().getCategoryType())
                .region(crewMember.getCrew().getRegion())
                .district(crewMember.getCrew().getDistrict())
                .crewImage(crewMember.getCrew().getCrewImage())
                .ratingAvg(crewMember.getCrew().getRatingAvg())
                .viewCount(crewMember.getCrew().getViewCount())
                .currentMemberCount(crewMember.getCrew().getCurrentMemberCount())
                .maxMembers(crewMember.getCrew().getMaxMembers())
                .role(crewMember.getRole())
                .build();
    }
}
