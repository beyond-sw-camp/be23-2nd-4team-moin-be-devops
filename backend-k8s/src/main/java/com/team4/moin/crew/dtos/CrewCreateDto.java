package com.team4.moin.crew.dtos;


import com.team4.moin.crew.domain.entitys.Crew;

import com.team4.moin.user.domain.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewCreateDto {
    @NotBlank(message = "크루명 입력은 필수입니다.")
    private String crewName;
    @NotNull(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
    private CategoryType categoryType;
    @NotBlank(message = "크루 활동지역 입력은 필수입니다.")
    private String region;
    private String district; // 구
    private String description;
    private String crewImage;
    @NotNull(message = "크루 정원 입력은 필수입니다.")
    private Integer maxMembers;
    private String profileImage;
    public Crew toEntity() {
        return Crew.builder()
                .name(this.crewName)
                .region(this.region)
                .district(this.district)
                .description(this.description)
                .categoryType(this.categoryType)
                .maxMembers(this.maxMembers)
                .build();
    }
}
