package com.team4.moin.Notification.dtos;

import com.team4.moin.Notification.domain.Notification;
import com.team4.moin.Notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationRes {
    private Long id;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private Long targetId;
    private LocalDateTime createdAt;

    public static NotificationRes from(Notification n) {
        return NotificationRes.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .targetId(n.getTargetId())
                .createdAt(n.getCreatedTime())
                .build();
    }
}
