package com.team4.moin.user.service;

import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.dtos.logindtos.SocialLoginResponse;
import com.team4.moin.user.dtos.tokendtos.AccessTokenDto;
import com.team4.moin.user.dtos.logindtos.GoogleProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GoogleService {
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    private final UserService userService;

    public GoogleService(UserService userService) {
        this.userService = userService;
    }


//     구글 로그인 실행 대장 메서드
    public User processGoogleLogin(String code) {
        // 1. 인가 코드로 구글 토큰 받기
        AccessTokenDto tokenDto = getAccessToken(code);

        // 2. 받은 토큰으로 구글 유저 프로필 가져오기
        GoogleProfileDto profileDto = getGoogleProfile(tokenDto.getAccessToken());

        // 3. 기존 회원인지 DB 조회 (Google의 'sub'가 providerId)
        User user = userService.getUserByProviderId(profileDto.getSub());
        // 4. 회원이 없으면(null) 신규 가입 시키기
        if (user == null) {
            user = userService.createGoogleUser(profileDto);
        }

        return user;
    }

    // 구글 액세스 토큰 요청
    public AccessTokenDto getAccessToken(String code) {
        RestClient restClient = RestClient.create();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenDto.class)
                .getBody();
    }

    // 구글 프로필 정보 요청
    public GoogleProfileDto getGoogleProfile(String token) {
        RestClient restClient = RestClient.create();
        return restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(GoogleProfileDto.class)
                .getBody();
    }
}