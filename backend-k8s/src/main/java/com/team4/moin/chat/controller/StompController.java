package com.team4.moin.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team4.moin.chat.dtos.ChatMessageDto;
import com.team4.moin.chat.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatMessageService chatMessageService;

    @Autowired
    public StompController(SimpMessageSendingOperations messageTemplate, ChatMessageService chatMessageService) {
        this.messageTemplate = messageTemplate;
        this.chatMessageService = chatMessageService;
    }

//   MessageMapping 어노테이션만 활용.
//    (1) MessageMapping이 특정 stompTopic에 발행되는 메시지를 받음.
//  (클라이언트가 서버에 넘긴 messageTopic을 여기서 받음)
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageDto chatMessageDto) throws JsonProcessingException {
        System.out.println(chatMessageDto.getMessage());
        try{
            chatMessageService.saveMessage(roomId, chatMessageDto);
            if (chatMessageDto.getImageUrls() != null && !chatMessageDto.getImageUrls().isEmpty()){
                messageTemplate.convertAndSend("/topic/" + roomId, chatMessageDto);
            }
        } catch (IllegalArgumentException e){
            System.out.println("공지 권한 없음 : " + e.getMessage());
        }
    }
}
