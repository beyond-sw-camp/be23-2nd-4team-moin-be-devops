package com.team4.moin.user.service;

import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.dtos.tokendtos.AccessTokenDto;
import com.team4.moin.user.dtos.logindtos.KakaoProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service

public class KakaoService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;


    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final UserService userService;

    public KakaoService(UserService userService) {
        this.userService = userService;
    }

    public User processKakaoLogin(String code) {
        // 1. 액세스 토큰 받기
        AccessTokenDto tokenDto = getAccessToken(code);

        // 2. 카카오 프로필 가져오기
        KakaoProfileDto profileDto = getKakaoProfile(tokenDto.getAccessToken());

        // 3. 기존 회원인지 조회
        User user = userService.getUserByProviderId(profileDto.getId());

        // 4. 없으면 가입, 있으면 그대로 진행
        if (user == null) {
            user = userService.createKakaoUser(profileDto);
        }

        return user;
    }

    // Token 요청
    public AccessTokenDto getAccessToken(String code){
        RestClient restClient = RestClient.create();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("grant_type", "authorization_code");

        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenDto.class)
                .getBody();
    }

    // Profile 요청
    public KakaoProfileDto getKakaoProfile(String token){
        RestClient restClient = RestClient.create();
        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(KakaoProfileDto.class)
                .getBody();
    }
}