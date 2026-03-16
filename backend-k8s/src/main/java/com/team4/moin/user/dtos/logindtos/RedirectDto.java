package com.team4.moin.user.dtos.logindtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedirectDto {
    private String code;
}
