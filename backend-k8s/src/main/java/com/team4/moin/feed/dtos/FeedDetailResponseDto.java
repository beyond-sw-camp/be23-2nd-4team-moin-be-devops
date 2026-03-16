package com.team4.moin.feed.dtos;

import com.team4.moin.feed.domain.Feed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedDetailResponseDto {
    private Long feedId;
    private String content;
    private String nickname;
    private String profileImageUrl;
    private List<String> imageUrls;     // 목록과 달리 '모든' 사진 리스트
    private Boolean isMyPost;
    private long likeCount;
    private boolean isLiked;
    private LocalDateTime createdTime;

    //  댓글 리스트
    private Page<CommentResponseDto> comments;

    public static FeedDetailResponseDto fromEntity(Feed feed, List<String> images, long likeCount, boolean isLiked, boolean isMyPost, Page<CommentResponseDto> comments) {
        return FeedDetailResponseDto.builder()
                .feedId(feed.getId())
                .content(feed.getContent())
                .nickname(feed.getUser().getNickname())
                .profileImageUrl(feed.getUser().getProfileImageUrl())
                .imageUrls(images)
                .likeCount(likeCount)
                .isMyPost(isMyPost)
                .isLiked(isLiked)
                .createdTime(feed.getCreatedTime())
                .comments(comments)
                .build();
    }
}
