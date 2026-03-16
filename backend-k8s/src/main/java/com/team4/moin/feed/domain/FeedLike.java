package com.team4.moin.feed.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"feed_id", "user_id"})}) // 테이블에 유니크 제약조거
public class FeedLike extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
