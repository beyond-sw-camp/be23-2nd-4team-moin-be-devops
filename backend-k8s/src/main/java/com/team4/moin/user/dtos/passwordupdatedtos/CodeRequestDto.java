package com.team4.moin.user.dtos.passwordupdatedtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeRequestDto {

    private String email; // 회원 이메일
    private String code; // 회원 이메일에 보내진 6자리 난수 값
}
