package com.team4.moin.chat.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MessageUpdateDto {
    private Long messageId;
    private Long roomId;
    private String newContent;
}
