package com.team4.moin.user.dtos.passwordupdatedtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailDto {

        private String toEmail; // 수신자 이메일 주소
        private String title; //메일 제목
        private String message; //메일 본문 내용

        public static MailDto from(String email, String code) {
            return MailDto.builder()
                    .toEmail(email)
                    .title("[Moin] 비밀번호 재설정 인증번호입니다.")
                    .message("귀하의 인증번호는 [" + code + "] 입니다. 5분 이내에 입력해주세요.")
                    .build();
        }

    public static MailDto forSignup(String email, String code) {
        return MailDto.builder()
                .toEmail(email)
                .title("[Moin] 회원가입 인증번호입니다.")
                .message("회원가입을 위한 인증번호는 [" + code + "] 입니다.")
                .build();
    }
}
