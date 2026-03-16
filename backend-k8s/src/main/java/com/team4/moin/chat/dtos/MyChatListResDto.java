package com.team4.moin.chat.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MyChatListResDto {
    private Long roomId;
    private String roomName;
//    private Long opponentUserId;
    private String opponentName;
    private String opponentImage;
    private String chatRoomImage;
    private String isGroupChat;
    private Long unReadCount; // 내가 읽지 않은 채팅 개수
    private Integer memberCount; // 해당 채팅방 인원수
    private LocalDateTime lastMessageTime; // 최신 메시지 전송 시간
    private String lastMessageContent;
}
