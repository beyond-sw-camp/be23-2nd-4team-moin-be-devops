package com.team4.moin.crew.dtos;


import com.team4.moin.user.domain.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewSearchDto {
    private String crewName;
    private String region;
    private CategoryType categoryType;
    private List<CategoryType> categoryTypes;
    private String district;

}
