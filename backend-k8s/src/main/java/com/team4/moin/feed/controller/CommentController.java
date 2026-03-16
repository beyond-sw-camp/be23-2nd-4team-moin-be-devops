package com.team4.moin.feed.controller;

import com.team4.moin.feed.dtos.CommentCreateDto;
import com.team4.moin.feed.dtos.CommentResponseDto;
import com.team4.moin.feed.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 등록
    @PostMapping("/create")
    public ResponseEntity<?> createComment(@RequestBody CommentCreateDto dto) {
        CommentResponseDto response = commentService.createComment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("댓글 생성이 완료되었습니다.");
    }

    // 삭제
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().body("댓글 삭제가 완료되었습니다.");
    }

    // 댓글 조회
    @GetMapping("/{feedId}")
    public ResponseEntity<Page<CommentResponseDto>> getComments(@PathVariable Long feedId, @PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(commentService.getComments(feedId, pageable));
    }

    // 좋아요
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Boolean> addLike(@PathVariable Long commentId) {
        commentService.addCommentLike(commentId);
        return ResponseEntity.ok(true);
    }

    //  좋아요 취소
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<Boolean> removeLike(@PathVariable Long commentId) {
        commentService.removeCommentLike(commentId);
        return ResponseEntity.ok(false);
    }
}