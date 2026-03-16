package com.team4.moin.feed.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Comment extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // 부모 댓글 (null이면 최상위 댓글) , 셀프 참조로 대댓글 기능 구현

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> children = new ArrayList<>(); // 자식 댓글들

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentLike> commentLikes = new ArrayList<>();


    public void update(String content) {
        this.content = content;
    }
}
