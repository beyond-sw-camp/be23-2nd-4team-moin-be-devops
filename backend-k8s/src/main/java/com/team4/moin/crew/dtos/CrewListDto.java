package com.team4.moin.crew.dtos;


import com.team4.moin.crew.domain.entitys.Crew;
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
public class CrewListDto {
    private Long crewId;
    private String crewName;
    private CategoryType categoryType;
    private String region;
    private String district;
    private String crewImage;
    private BigDecimal ratingAvg;
    private Boolean favorite;
    private Long viewCount;
    private Integer currentMemberCount;
    private Integer maxMembers;

    public static CrewListDto fromEntity(Crew crew) {
        return CrewListDto.builder()
                .crewId(crew.getId())
                .crewName(crew.getName())
                .categoryType(crew.getCategoryType())
                .region(crew.getRegion())
                .district(crew.getDistrict())
                .crewImage(crew.getCrewImage())
                .ratingAvg(crew.getRatingAvg())
                .favorite(false)
                .viewCount(crew.getViewCount())
                .currentMemberCount(crew.getCurrentMemberCount())
                .maxMembers(crew.getMaxMembers())
                .build();
    }
}
