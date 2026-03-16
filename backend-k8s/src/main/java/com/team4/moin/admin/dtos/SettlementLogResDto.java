package com.team4.moin.admin.dtos;

import com.team4.moin.fee.domain.SettlementLog;
import com.team4.moin.fee.domain.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementLogResDto {
    private Long logId;
    private Long meetingId;
    private String meetingName;
    private String ownerEmail;
    private Long amount;
    private String bankName;
    private String accountNumber;
    private SettlementStatus status;
    private LocalDateTime settledAt;

    public static SettlementLogResDto fromEntity(SettlementLog log) {
        return SettlementLogResDto.builder()
                .logId(log.getId())
                .meetingId(log.getMeeting().getId())
                .meetingName(log.getMeeting().getName())
                .ownerEmail(log.getUser().getEmail())
                .amount(log.getAmount())
                .bankName(log.getBankName())
                .accountNumber(log.getAccountNumber())
                .status(log.getIsSettled())
                .settledAt(log.getCreatedTime())
                .build();
    }


}
