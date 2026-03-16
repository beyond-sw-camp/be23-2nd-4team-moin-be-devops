package com.team4.moin.user.dtos.tokendtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessTokenDto {
    @JsonProperty("access_token") // 데이터 받을 때 언더스크롤 인식
    private String accessToken;
    @JsonProperty("expires_in")
    private String expiresIn;
    private String scope;
    @JsonProperty("id_token")
    private String idToken;
}
