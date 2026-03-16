package com.team4.moin.Notification.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private User user;  // 알림받는사람
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;
    @Column(nullable = false)
    private String message;
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    @Column(nullable = false)
    private String referenceType;  // meeting / chat / ...
    @Column(nullable = false)
    private Long targetId;  // 모임ID, 채팅ID, 유저Id ..

    public void read(){
        this.isRead = true;
    }


}
