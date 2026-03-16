package com.team4.moin.meeting.dtos;

import com.team4.moin.fee.domain.FeeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MeetingFeeLogDto {
    private Long meetingId;
    private String email;
    private Long amount;
    private LocalDateTime createdAt;

    public MeetingFeeLogDto fromEntity(FeeLog feeLog){
        return MeetingFeeLogDto.builder()
                .meetingId(feeLog.getMeetingId().getId())
                .amount(feeLog.getAmount())
                .email(feeLog.getEmail())
                .createdAt(feeLog.getCreatedTime())
                .build();
    }
}
