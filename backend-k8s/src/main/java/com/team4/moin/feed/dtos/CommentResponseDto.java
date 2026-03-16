package com.team4.moin.feed.dtos;

import com.team4.moin.feed.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponseDto {
    private Long commentId;
    private String content;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime createdTime;
    private long likeCount;
    private boolean isLiked;
    private Boolean isMyComment;
    private Long parentId;
    private CommentResponseDto reply;

    public static CommentResponseDto fromEntity(Comment comment, long likeCount, boolean isLiked, boolean isMyComment, CommentResponseDto reply) {
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getUser().getNickname())
                .profileImageUrl(comment.getUser().getProfileImageUrl())
                .createdTime(comment.getCreatedTime())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .isMyComment(isMyComment)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .reply(reply) // 대댓글 리스트가 들어감
                .build();
    }
}
