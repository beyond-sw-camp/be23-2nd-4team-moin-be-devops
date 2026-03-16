package com.team4.moin.Notification.repository;

import com.team4.moin.Notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자별 알림 목록 최신순
    List<Notification> findByUser_IdOrderByCreatedTimeDesc(Long userId);

    // 본인 알림인지 확인용 (읽음 처리 시)
    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);
}
