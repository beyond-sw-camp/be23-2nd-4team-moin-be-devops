package com.team4.moin.chat.dtos;

import com.team4.moin.chat.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageDto {
    private Long messageId;
    private Long roomId;
    private String senderEmail;
    private String senderNickname;
    private String senderProfileImage;
    private String message;

    @Builder.Default
    private MessageType messageType = MessageType.NORMAL;
    @Builder.Default
    private String delYn = "N";
    @Builder.Default
    private String editedYn = "N";

    private List<String> imageUrls;

    @Builder.Default
    private List<String> chatImageUrlList = new ArrayList<>();
    
//    읽지 않은 사람 수
    private Integer unreadCount;

}
