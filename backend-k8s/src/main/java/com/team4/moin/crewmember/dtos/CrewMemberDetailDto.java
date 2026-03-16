package com.team4.moin.crewmember.dtos;

import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.user.domain.enums.CategoryType;
import com.team4.moin.user.domain.enums.Gender;
import com.team4.moin.user.domain.enums.Mbti;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMemberDetailDto {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private Integer age;
    private Gender gender;
    private Mbti mbti;
    private BigDecimal mannerScore;
    private List<CategoryType> categoryTypes;

    private Long crewMemberId;
    private CrewRole crewRole;
    private LocalDateTime joinedAt; // 크루 가입일
    private boolean isMe;

    public static CrewMemberDetailDto fromEntity(CrewMember crewMember, String currentEmail) {
        var user = crewMember.getUser();

        // 나이 계산 (birthDate가 null일 수 있으므로 방어 처리)
        Integer age = null;
        if (user.getBirthDate() != null) {
            age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        }

        List<CategoryType> categoryTypes = user.getCategories().stream()
                .map(c -> c.getCategoryType())
                .collect(Collectors.toList());

        boolean isMe = user.getEmail().equals(currentEmail);

        return CrewMemberDetailDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .age(age)
                .gender(user.getGender())
                .mbti(user.getMbti())
                .mannerScore(user.getMannerScore())
                .categoryTypes(categoryTypes)
                .crewMemberId(crewMember.getId())
                .crewRole(crewMember.getRole())
                .joinedAt(crewMember.getCreatedTime())
                .isMe(isMe)
                .build();
    }
}
