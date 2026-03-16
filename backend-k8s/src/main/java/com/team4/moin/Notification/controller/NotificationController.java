package com.team4.moin.Notification.controller;

import com.team4.moin.Notification.dtos.NotificationRes;
import com.team4.moin.Notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

//    sse 연결
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.subscribe();
    }

//    sse 연결 해제
    @DeleteMapping("/disconnect")
    public ResponseEntity<Void> disconnect() {
        notificationService.disconnect();
        return ResponseEntity.ok().build();
    }

//    알림 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<List<NotificationRes>> getList() {
        return ResponseEntity.ok(notificationService.getList());
    }

//    알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(@PathVariable Long notificationId) {
        notificationService.delete(notificationId);
        return ResponseEntity.noContent().build();
    }
}

