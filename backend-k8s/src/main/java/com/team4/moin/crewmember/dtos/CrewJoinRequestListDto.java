package com.team4.moin.crewmember.dtos;

import com.team4.moin.crewmember.domain.entity.CrewMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewJoinRequestListDto {
    private Long joinId;
    // 신청자 정보
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    //신청 메시지
    private String joinMessage;
    //가입 신청 시간
    private LocalDateTime createdTime;
    private BigDecimal mannerScore;

    public static CrewJoinRequestListDto fromEntity(CrewMember crewMember) {
        CrewJoinRequestListDto build = CrewJoinRequestListDto.builder()
                .joinId(crewMember.getId())
                .userId(crewMember.getUser().getId())
                .nickname(crewMember.getUser().getNickname())
                .profileImageUrl(crewMember.getUser().getProfileImageUrl())
                .mannerScore(crewMember.getUser().getMannerScore())
                .joinMessage(crewMember.getJoinMessage())
                .createdTime(crewMember.getCreatedTime())
                .build();
        return build;
    }
}