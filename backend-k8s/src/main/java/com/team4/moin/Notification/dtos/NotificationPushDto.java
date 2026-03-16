package com.team4.moin.Notification.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


//Redis Pub/Sub으로 전달할 알림 메시지 DTO
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationPushDto {
    // 알림을 받을 사용자 ID
    private Long receiverUserId;
    // 전송할 알림 내용
    private NotificationRes payload;
}
