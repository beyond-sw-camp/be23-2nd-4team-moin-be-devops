package com.team4.moin.chat.dtos;

import com.team4.moin.chat.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatImageDto {
    private Long messageId;
    private Long roomId;
    private String senderEmail;
    private String message;
//    @Builder.Default
//    private MessageType messageType = MessageType.NORMAL;
    @Builder.Default
    private MessageType messageType = MessageType.IMAGE;
    @Builder.Default
    private String delYn = "N";
    @Builder.Default
    private String editedYn = "N";

    @Builder.Default
    private List<String> images = new ArrayList<>();

}
