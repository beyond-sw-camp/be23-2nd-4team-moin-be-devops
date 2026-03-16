package com.team4.moin.user.dtos.logindtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMyinfoDto {
    private String email;
    private String nickname;
}
