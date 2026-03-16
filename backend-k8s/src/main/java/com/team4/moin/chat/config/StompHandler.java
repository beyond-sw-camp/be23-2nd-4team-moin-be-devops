package com.team4.moin.chat.config;


import com.team4.moin.chat.domain.ChatParticipant;
import com.team4.moin.chat.service.ChatMessageService;
import com.team4.moin.chat.service.ChatService;
import com.team4.moin.chat.service.ChatValidatorService;
import com.team4.moin.chat.service.ReadStatusService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final ChatValidatorService chatValidatorService;
    private final ReadStatusService readStatusService;

    @Autowired
    public StompHandler(ChatValidatorService chatValidatorService, ReadStatusService readStatusService) {
        this.chatValidatorService = chatValidatorService;
        this.readStatusService = readStatusService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //        사용자 요청이 accessor 안에 담기게됨.
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()){
            System.out.println("connect요청 시 토큰 유효성 검증");
//            accessor에서 토큰 받아옴.
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
//            토큰 검증
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("토큰 검증 완료");
        }
        if (StompCommand.SUBSCRIBE == accessor.getCommand()){
            System.out.println("subscribe 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String email = claims.getSubject();
            String destination = accessor.getDestination(); ///

            // /topic/read/{roomId} 구독은 roomId 파싱 없이 토큰 검증만 하고 통과
            if (destination.startsWith("/topic/read/")) {
                System.out.println("READ_ACK 구독 요청: " + destination);
                return message;
            }

            String roomId = accessor.getDestination().split("/")[2]; // 요청하고자 하는 url /topic/${this.roomId}
            try {
                ChatParticipant chatParticipant = chatValidatorService.validateAndGetParticipant(email, Long.parseLong(roomId));
                readStatusService.markRoomMessagesAsRead(chatParticipant);
                System.out.println("읽음 처리 완료: " + email + " in room " + roomId);
            } catch (IllegalArgumentException | jakarta.persistence.EntityNotFoundException e) {
                System.err.println("권한 검증 실패: " + e.getMessage());
                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
            } catch (Exception e) {
                System.err.println("읽음 처리 실패: " + e.getMessage());
                // 읽음 처리 실패해도 구독은 계속 진행
            }
        }
        return message;
    }
}