package com.team4.moin.crew.dtos;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.user.domain.enums.CategoryType;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrewDetailDto {
    private Long crewId;
    private String crewName;
    private CategoryType categoryType;
    private String region;
    private String district;
    private String description;
    private String crewImage;

    private Integer currentMemberCount;
    private Integer maxMembers;
    private Integer favoriteCount;
    private BigDecimal ratingAvg;
    private Boolean favorite;
    private Long viewCount;

    public static CrewDetailDto fromEntity(Crew crew) {
        return CrewDetailDto.builder()
                .crewId(crew.getId())
                .crewName(crew.getName())
                .categoryType(crew.getCategoryType())
                .region(crew.getRegion())
                .district(crew.getDistrict())
                .description(crew.getDescription())
                .crewImage(crew.getCrewImage())
                .currentMemberCount(crew.getCurrentMemberCount())
                .maxMembers(crew.getMaxMembers())
                .favoriteCount(crew.getFavoriteCount())
                .ratingAvg(crew.getRatingAvg())
                .favorite(false)
                .viewCount(crew.getViewCount())
                .build();
    }
}