package com.team4.moin.chat.dtos;

import com.team4.moin.chat.domain.ChatRoom;
import com.team4.moin.crew.domain.entitys.Crew;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatRoomListResDto {
    private Long roomId;
    private String roomName;
    private String crewName;

    public static ChatRoomListResDto fromEntity(ChatRoom chatRoom){
        return ChatRoomListResDto.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .crewName(chatRoom.getCrew().getName())
                .build();
    }
}
