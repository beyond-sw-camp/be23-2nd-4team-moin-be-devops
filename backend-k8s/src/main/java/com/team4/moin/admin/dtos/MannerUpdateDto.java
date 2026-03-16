package com.team4.moin.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MannerUpdateDto {
    private Long userId;
    private BigDecimal delta; //  3.0를 보내면 +3.0, -3.0을 보내면 -3.0
}
