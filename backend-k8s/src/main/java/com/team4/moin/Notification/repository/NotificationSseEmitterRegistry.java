package com.team4.moin.Notification.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSseEmitterRegistry {
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    // userId 기준으로 SSE 구독 등록
    public void addSseEmitter(String userKey, SseEmitter sseEmitter) {
        emitterMap.put(userKey, sseEmitter);
    }

    // userKey로 등록된 Emitter 조회
    public SseEmitter getEmitter(String userKey) {
        return emitterMap.get(userKey);
    }

    // 구독 해제
    public void removeEmitter(String userKey) {
        emitterMap.remove(userKey);
    }
}
