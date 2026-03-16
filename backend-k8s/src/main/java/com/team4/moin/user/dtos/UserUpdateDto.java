package com.team4.moin.user.dtos;

import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.Gender;
import com.team4.moin.user.domain.enums.Mbti;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDto {
    private Long id;              // 식별용 (수정 시 입력받지는 않음)
    private String nickname;
    private Mbti mbti;
    private LocalDate birthDate;
    private Gender gender;
    private String city;
    private String district;
    private String street;
    private String profileImageUrl; // 프론트에서 현재 이미지를 보여주기 위해 필수!
    private List<String> categoryNames;

    public static UserUpdateDto fromEntity(User user) {
        return UserUpdateDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .profileImageUrl(user.getProfileImageUrl())
                // Address 객체가 null일 경우를 대비한 안전한 처리
                .city(user.getAddress() != null ? user.getAddress().getCity() : null)
                .street(user.getAddress() != null ? user.getAddress().getStreet() : null)
                // 카테고리 리스트 변환 (Enum 타입의 name 추출)
                .categoryNames(user.getCategories() != null ?
                        user.getCategories().stream()
                                .map(category -> category.getCategoryType().name())
                                .collect(Collectors.toList()) : null)
                .build();
    }
}