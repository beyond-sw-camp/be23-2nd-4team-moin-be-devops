package com.team4.moin.user.dtos.logindtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialLoginResponse {

    private boolean newUser;        // true = 신규, false = 기존

    private String accessToken;     // 기존 회원일 때만 값 세팅
    private String refreshToken;    // 기존 회원일 때만 값 세팅

    private String email;
    private String nickname;
    private String provider;        // "GOOGLE", "KAKAO"
    private String providerId;
    private String profileImageUrl;
}
