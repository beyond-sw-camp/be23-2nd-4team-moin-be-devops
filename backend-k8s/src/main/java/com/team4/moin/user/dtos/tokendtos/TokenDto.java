package com.team4.moin.user.dtos.tokendtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDto {

private String accessToken;
private String refreshToken;
}
