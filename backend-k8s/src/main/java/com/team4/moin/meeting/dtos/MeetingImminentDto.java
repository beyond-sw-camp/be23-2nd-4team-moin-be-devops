package com.team4.moin.meeting.dtos;

import com.team4.moin.meeting.domain.entitys.Meeting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingImminentDto {
    private String profileImage;
    private Long meetingId;
    private String meetingName;
    private LocalDateTime meetingAt; // 일정 표시용

    private Integer currentMemberCount; // 현재 인원
    private Integer maxMembers;         // 최대 정원
    private Integer remainingSpots;     // 남은 자리 수 (백엔드에서 계산해서 전달)

    public static MeetingImminentDto fromEntity(Meeting meeting) {
        // 남은 자리 계산 (정원이 없는 모임 방지용 안전 처리)
        int max = (meeting.getMaxMembers() != null) ? meeting.getMaxMembers() : 0;
        int remaining = max > 0 ? (max - meeting.getCurrentMemberCount()) : 0;

        return MeetingImminentDto.builder()
                .meetingId(meeting.getId())
                .profileImage(meeting.getProfileImage())
                .meetingName(meeting.getName())
                .meetingAt(meeting.getMeetingAt())
                .currentMemberCount(meeting.getCurrentMemberCount())
                .maxMembers(meeting.getMaxMembers())
                .remainingSpots(remaining) // 프론트엔드는 이 값만 보고 "n자리 남음" 렌더링 가능
                .build();
    }
}