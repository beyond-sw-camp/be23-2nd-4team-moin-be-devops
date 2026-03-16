package com.team4.moin.feed.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentCreateDto {
    private Long feedId;
    private String content;
    private Long parentId; // 일반 댓글일 경우 null, 대댓글일 경우 부모 댓글의 ID
}
