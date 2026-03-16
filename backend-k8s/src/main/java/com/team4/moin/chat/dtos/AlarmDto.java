package com.team4.moin.chat.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AlarmDto {
    private Long userId;
    private Long roomId;
    private String roomName;
    private String lastMessage;
    private String senderNickname;
}
