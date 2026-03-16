package com.team4.moin.user.dtos;

import com.team4.moin.user.domain.enums.Gender;
import com.team4.moin.user.domain.enums.Mbti;
import com.team4.moin.user.domain.enums.Provider;
import com.team4.moin.user.domain.entitys.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSignupDto {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}",
            message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
//?=.*[0-9]) : 숫자가 포함 // (?=.*[a-zA-Z]) : 대소문자 필수 //(?=.*\W) : 특수문자 필수 //(?=\\S+$): 공백(스페이스바)은 포함될 수 없음 //.{8,16}: "전체 길이는 8자 이상"
    private String password;
    private String nickname;
    private String city;
    private String district;
    private String street;
    @NotBlank(message = "mbti는 필수 입력 값입니다.")
    private String mbti;
    private LocalDate birthDate;
    @NotBlank(message = "성별은 필수 입력 값입니다.")
    private String gender; // 프론트에서 "MALE" 또는 "FEMALE"로 전달
    private List<String> categories; // 프론트에서 클릭으로 카테고리 설정

    private String profileImage;
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .birthDate(this.birthDate)
                .gender(Gender.valueOf(this.gender.toUpperCase()))
                .provider(Provider.LOCAL)
                .mbti(Mbti.valueOf(this.mbti.toUpperCase()))
                .build();
    }
}
