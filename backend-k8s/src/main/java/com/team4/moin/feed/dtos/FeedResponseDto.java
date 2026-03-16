package com.team4.moin.feed.dtos;

import com.team4.moin.feed.domain.Feed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedResponseDto {
    private Long feedId;
    private String content;
    private String nickname;
    private String profileImageUrl;
    private List<String> imageUrls;

    // 카운트 및 상태 정보
    private long likeCount;     // 좋아요 총 개수
    private long commentCount;  // 댓글 + 대댓글 총 개수
    private boolean isLiked;    // 내가 좋아요를 눌렀는지 여부
    private Boolean isMyPost;

    private LocalDateTime createdTime;

    public static FeedResponseDto of(Feed feed, List<String> images, long likeCount, long commentCount, boolean isLiked, boolean isMyPost) {
        return FeedResponseDto.builder()
                .feedId(feed.getId())
                .content(feed.getContent())
                .nickname(feed.getUser().getNickname())
                .profileImageUrl(feed.getUser().getProfileImageUrl())
                // 첫 번째 사진을 썸네일로 사용 (사진이 없을 경우 처리)
                .imageUrls(images.isEmpty() ? null : images)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .isMyPost(isMyPost)
                .createdTime(feed.getCreatedTime())
                .build();
    }
}
