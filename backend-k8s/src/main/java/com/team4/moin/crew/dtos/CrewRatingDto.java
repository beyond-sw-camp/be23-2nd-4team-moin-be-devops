package com.team4.moin.crew.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewRatingDto {
    private Long crewId;
    @NotNull
    @DecimalMin(value = "1.0", inclusive = true)
    @DecimalMax(value = "5.0", inclusive = true)
    private BigDecimal score;

}
