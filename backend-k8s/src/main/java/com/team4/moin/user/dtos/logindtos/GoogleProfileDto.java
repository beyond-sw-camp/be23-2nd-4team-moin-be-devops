package com.team4.moin.user.dtos.logindtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleProfileDto {
    private String sub;    // 구글의 고유 유저 ID
    private String email;
    private String name;
}
