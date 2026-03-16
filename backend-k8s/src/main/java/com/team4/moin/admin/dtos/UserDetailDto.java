package com.team4.moin.admin.dtos;

import com.team4.moin.user.domain.entitys.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailDto {
    private Long id;
    private String nickName;
    private String email;
    private BigDecimal mannerScore;
    private String delYn;
    public static UserDetailDto fromEntity(User user){
        return UserDetailDto.builder()
                .id(user.getId())
                .nickName(user.getNickname())
                .email(user.getEmail())
                .mannerScore(user.getMannerScore())
                .delYn(user.getDelYn())
                .build();
    }

}
