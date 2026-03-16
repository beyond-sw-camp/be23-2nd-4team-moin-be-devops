package com.team4.moin.Notification.service;

import com.team4.moin.Notification.domain.Notification;
import com.team4.moin.Notification.domain.NotificationType;
import com.team4.moin.Notification.dtos.NotificationRes;
import com.team4.moin.Notification.repository.NotificationRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSseService sseService;
    private final UserRepository userRepository;

    // 유저 정보 찾기 (트랜잭션 범위 내에서만 사용)
    public Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
    }
    @Transactional(readOnly = true)
    public User getCurrentUserWithDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailWithDetails(email, "No")
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
    }
    /**
     * SSE 알림 구독 연결.
     * 트랜잭션을 걸지 않음 → DB 연결을 오래 잡지 않아 커넥션 풀 고갈을 방지.
     */
    public SseEmitter subscribe() {
        Long userId = getCurrentUserId();
        return sseService.subscribe(userId);
    }

    /**
     * SSE 알림 연결 해제.
     * 트랜잭션을 걸지 않음.
     */
    public void disconnect() {
        try {
        Long userId = getCurrentUserId();
        sseService.disconnect(userId);
    } catch (EntityNotFoundException | NullPointerException e) {
        // 이미 로그아웃되거나 다른 파드로 요청이 간 경우 무시
        log.warn("SSE disconnect 중 유저 정보 없음 - 무시: {}", e.getMessage());
    }
    }

    // 로그인 사용자의 알림 목록 최신순 조회
    @Transactional(readOnly = true)
    public List<NotificationRes> getList() {
        Long userId = getCurrentUserId();
        List<Notification> list = notificationRepository.findByUser_IdOrderByCreatedTimeDesc(userId);
        return list.stream()
                .map(NotificationRes::from)
                .collect(Collectors.toList());
    }


    //  알림 읽음 처리 (본인 알림만 가능)
    @Transactional
    public void markAsRead(Long notificationId) {
        Long userId = getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));
        notification.read();
    }

    @Transactional
    public void send(
            User user,
            NotificationType type,
            String referenceType,
            Long targetId,
            String message
    ) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .referenceType(referenceType)
                .targetId(targetId)
                .message(message)
                .build();

        notificationRepository.save(notification);

        sseService.send(
                user.getId(),
                NotificationRes.from(notification)
        );
    }
    public void delete(Long notificationId) {
        Long userId = getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));
        notificationRepository.delete(notification);
    }
}

