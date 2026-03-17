package com.team4.moin.crewmember.dtos;

import java.time.LocalDateTime;

import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMemberListDto {
    private Long crewMemberId;
    private Long userId; //
    private CrewRole role;
    private String nickname;
    private String profileImageUrl;
    private boolean isMe; //
    private LocalDateTime joinedAt;

    public static CrewMemberListDto fromEntity(CrewMember crewMember) {
        return CrewMemberListDto.builder()
                .crewMemberId(crewMember.getId())
                .userId(crewMember.getUser().getId())
                .role(crewMember.getRole())
                .nickname(crewMember.getUser().getNickname())
                .profileImageUrl(crewMember.getUser().getProfileImageUrl())
                .isMe(false)
                .joinedAt(crewMember.getCreatedTime())
                .build();
    }

    public static CrewMemberListDto fromEntity(CrewMember member, String currentEmail) {
        boolean isMe = member.getUser().getEmail().equals(currentEmail);
        return CrewMemberListDto.builder()
                .crewMemberId(member.getId())
                .userId(member.getUser().getId())
                .role(member.getRole())
                .nickname(member.getUser().getNickname())
                .profileImageUrl(member.getUser().getProfileImageUrl())
                .isMe(isMe)
                .joinedAt(member.getCreatedTime())
                .build();
    }
}
