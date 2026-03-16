package com.team4.moin.feed.domain;

import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"comment_id", "user_id"})}) // 유니크 설정
public class CommentLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
