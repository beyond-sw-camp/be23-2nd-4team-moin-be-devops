package com.team4.moin.feed.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class FeedImage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed; // 어느 게시글의 사진인지

    @Column(nullable = false)
    private String imageUrl; // S3에서 발급받은 URL

    @Column(nullable = false)
    private int sequence; // 사진 순서 (0, 1, 2... 순서대로 저장)
}
