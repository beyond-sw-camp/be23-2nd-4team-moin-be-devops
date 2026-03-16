package com.team4.moin.user.dtos.logindtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 응답 데이터 중 DTO에 없는 필드는 무시 (에러 방지)
public class KakaoProfileDto {

    private String id; // 카카오가 부여한 고유 번호 -> User 엔티티의 providerId로 매핑
    private KakaoAccount kakao_account;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email; // 사용자 이메일 -> User 엔티티의 email로 매핑
        private Profile profile;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String nickname; // 사용자 닉네임 -> User 엔티티의 nickname으로 매핑
    }
}