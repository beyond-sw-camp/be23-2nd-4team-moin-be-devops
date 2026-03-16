package com.team4.moin.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.moin.chat.dtos.ChatMessageDto;
import com.team4.moin.chat.dtos.ChatReadAckDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;

    @Autowired
    public RedisPubSubService(StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
    }

    public void publish(String channel, String message){
        stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String payload = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("Redis onMessage channel: " + channel); // 채널 확인
        System.out.println("Redis onMessage payload: " + payload);
        try {
            if (channel.equals("chat-read")){
                ChatReadAckDto ackDto = objectMapper.readValue(payload, ChatReadAckDto.class);
                messageTemplate.convertAndSend("/topic/read/" + ackDto.getRoomId(), ackDto);
            } else {
                // 기존 채팅 메시지 처리
                ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
                messageTemplate.convertAndSend("/topic/" + chatMessageDto.getRoomId(), chatMessageDto);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
