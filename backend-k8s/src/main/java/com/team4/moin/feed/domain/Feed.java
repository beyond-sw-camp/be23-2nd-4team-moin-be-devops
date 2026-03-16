package com.team4.moin.feed.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.user.domain.entitys.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Feed extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew; // 어느 크루의 사진첩인지

    @Column(columnDefinition = "TEXT")
    private String content; // 게시글 내용

    // orphanRemoval = true: 게시글 수정 시 기존 사진을 리스트에서 제거하면 DB에서도 삭제됨
//    케스케이드로 게시글 삭제시 사진들도 함께 삭제
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC") // 사진 순서 정렬
    @Builder.Default
    private List<FeedImage> images = new ArrayList<>();

    //  댓글도 함께 삭제
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // 피드 좋아요 함께 삭제
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeedLike> feedLikes = new ArrayList<>();

    public void updateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다.");
        }
        this.content = content;
    }
}

