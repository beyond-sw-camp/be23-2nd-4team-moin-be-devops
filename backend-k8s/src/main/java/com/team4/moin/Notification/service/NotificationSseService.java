package com.team4.moin.Notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.moin.Notification.dtos.NotificationPushDto;
import com.team4.moin.Notification.dtos.NotificationRes;
import com.team4.moin.Notification.repository.NotificationSseEmitterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationSseService implements MessageListener {

    private static final String NOTIFICATION_CHANNEL = "notification-channel";
    private static final long SSE_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);

    private final NotificationSseEmitterRegistry emitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?>[] heartbeatRef = new ScheduledFuture<?>[1];

    public NotificationSseService(
            NotificationSseEmitterRegistry emitterRegistry,
            ObjectMapper objectMapper,
            @Qualifier("ssePubSub") RedisTemplate<String, Object> redisTemplate
    ) {
        this.emitterRegistry = emitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * SSE 구독: 클라이언트가 /notifications/connect 로 연결 시 호출.
     * userId 기준으로 Emitter를 등록하고, 타임아웃/완료 시 제거한다.
     */
    public SseEmitter subscribe(Long userId) {
        String userKey = String.valueOf(userId);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
// heartbeat 스케줄러
        ScheduledFuture<?>[] heartbeatRef = new ScheduledFuture<?>[1];

        heartbeatRef[0] = scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                heartbeatRef[0].cancel(false);
                emitterRegistry.removeEmitter(userKey);
            }
        }, 0, 30, TimeUnit.SECONDS);
        emitter.onTimeout(() -> emitterRegistry.removeEmitter(userKey));
        emitter.onCompletion(() -> emitterRegistry.removeEmitter(userKey));
        emitter.onError(e -> emitterRegistry.removeEmitter(userKey));

        emitterRegistry.addSseEmitter(userKey, emitter);

        // 연결 직후 클라이언트에 연결 완료 메시지 전송 (로딩 확인용)
        try {
            String connectedMessage = objectMapper.writeValueAsString(
                    java.util.Map.of("message", "알림 연결이 완료되었습니다.", "event", "connected"));
            emitter.send(SseEmitter.event().name("connected").data(connectedMessage));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * SSE 연결 해제: 레지스트리에서 제거하고 Emitter를 완료시켜 클라이언트 연결을 닫는다.
     */
    public void disconnect(Long userId) {
        String userKey = String.valueOf(userId);
        SseEmitter emitter = emitterRegistry.getEmitter(userKey);
        if (emitter != null) {
            emitterRegistry.removeEmitter(userKey);
            emitter.complete();
        }
    }

    /**
     * 해당 사용자에게 실시간 알림 전송.
     * 현재 서버에 해당 사용자의 Emitter가 있으면 바로 전송하고,
     * 없으면 Redis에 publish하여 해당 사용자가 연결된 서버에서 전송하도록 한다.
     */
    public void send(Long userId, NotificationRes payload) {
        String userKey = String.valueOf(userId);
        SseEmitter emitter = emitterRegistry.getEmitter(userKey);

        try {
            String data = objectMapper.writeValueAsString(payload);
            if (emitter != null) {
                emitter.send(SseEmitter.event().name("notification").data(data));
            } else {
                NotificationPushDto pushDto = NotificationPushDto.builder()
                        .receiverUserId(userId)
                        .payload(payload)
                        .build();
                String message = objectMapper.writeValueAsString(pushDto);
                redisTemplate.convertAndSend(NOTIFICATION_CHANNEL, message);
            }
        } catch (IOException e) {
            throw new RuntimeException("SSE 알림 전송 실패: userId=" + userId, e);
        }
    }

    /**
     * Redis notification-channel 구독: 다른 인스턴스에서 publish한 알림을 받아
     * 현재 서버에 연결된 사용자에게 전달한다.
     */
    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        try {
            NotificationPushDto dto = objectMapper.readValue(message.getBody(), NotificationPushDto.class);
            String userKey = String.valueOf(dto.getReceiverUserId());
            SseEmitter emitter = emitterRegistry.getEmitter(userKey);
            if (emitter == null) {
                return;
            }
            String data = objectMapper.writeValueAsString(dto.getPayload());
            emitter.send(SseEmitter.event().name("notification").data(data));
        } catch (IOException e) {
            throw new RuntimeException("Redis 알림 메시지 처리 실패", e);
        }
    }
}
