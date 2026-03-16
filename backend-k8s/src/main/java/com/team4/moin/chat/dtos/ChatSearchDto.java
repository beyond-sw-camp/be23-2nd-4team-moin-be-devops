package com.team4.moin.chat.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatSearchDto {
    private String keyword; // 메시지 내용 검색어
    private Long lastMessageId; // 마지막으로 받은 메시지 id
}
