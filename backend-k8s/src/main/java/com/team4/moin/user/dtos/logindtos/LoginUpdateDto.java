package com.team4.moin.user.dtos.logindtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class LoginUpdateDto {
    private String email;
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;
    private String city;
    private String district;
    private String street;
    private LocalDate birthDate;
    @NotBlank(message = "성별은 필수 입력값입니다.")
    private String gender;
    @NotEmpty(message = "관심사는 필수 입력값입니다.")
    private List<String> categories;
    private String profileImage;
    private String mbti;

}
