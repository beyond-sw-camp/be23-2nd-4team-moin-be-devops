package com.team4.moin.crew.dtos;


import com.team4.moin.user.domain.enums.CategoryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewUpdateDto {
    @NotNull
    private Long crewId;
    private String crewName;
    private String description;
    private String region;
    private String crewImage;
    private Integer maxMembers;
    private CategoryType categoryType;
    private String district;
}
