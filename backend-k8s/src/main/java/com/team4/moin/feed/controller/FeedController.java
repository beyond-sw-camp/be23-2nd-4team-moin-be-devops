package com.team4.moin.feed.controller;

import com.team4.moin.feed.dtos.FeedCreateDto;
import com.team4.moin.feed.dtos.FeedDetailResponseDto;
import com.team4.moin.feed.dtos.FeedResponseDto;
import com.team4.moin.feed.dtos.FeedTextUpdateDto;
import com.team4.moin.feed.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    @Autowired
    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

//    Presigned URL 발급
    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String fileName) {
        String url = feedService.getPresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }

//    이미지로 받지 않고 url 문자열
    @PostMapping("/create")
    public ResponseEntity<?> createFeed(@RequestBody FeedCreateDto dto) {
        return ResponseEntity.ok(feedService.createFeed(dto));
    }
    //  피드 내용(텍스트)만 수정
    @PutMapping("/content/update")
    public ResponseEntity<?> updateContent(@RequestBody FeedTextUpdateDto dto) {
        feedService.updateFeedContent(dto);
        return ResponseEntity.ok("수정이 완료되었습니다.");
    }

    //  피드 이미지들만 수정 (전체 교체)
    @PutMapping("/images/update/{feedId}")
    public ResponseEntity<?> updateImages(@PathVariable Long feedId, @RequestBody List<String> images) {
        feedService.updateFeedImages(feedId, images);
        return ResponseEntity.ok("수정이 완료되었습니다.");
    }

//  피드 좋아요
    @PostMapping("/like/{feedId}")
    public ResponseEntity<Boolean> addLike(@PathVariable Long feedId) {
        feedService.addLike(feedId);
        return ResponseEntity.ok(true);
    }

// 피드 좋아요 취소
    @DeleteMapping("/like/{feedId}")
    public ResponseEntity<Boolean> removeLike(@PathVariable Long feedId) {
        feedService.removeLike(feedId);
        return ResponseEntity.ok(false);
    }
    //  크루별 피드 목록 조회 (사진첩)
    @GetMapping("/crew/{crewId}")
    public ResponseEntity<?> getCrewFeeds(@PathVariable Long crewId, Pageable pageable) {
        Page<FeedResponseDto> dto = feedService.getFeeds(crewId, pageable);
        return ResponseEntity.ok(dto);
    }

    //  피드 상세 조회 (피드 내용 + 모든 사진 + 댓글 페이징)
    @GetMapping("/{feedId}")
    public ResponseEntity<?> getFeedDetail(@PathVariable Long feedId, @PageableDefault(size = 10) Pageable commentPageable) {
        FeedDetailResponseDto dto = feedService.getFeedDetail(feedId, commentPageable);
        return ResponseEntity.ok(dto);
    }
    @DeleteMapping("/delete/{feedId}")
    public ResponseEntity<String> deleteImage(@PathVariable Long feedId) {
        feedService.deleteFeedImage(feedId);

        return ResponseEntity.ok("피드가 성공적으로 삭제되었습니다.");
    }
    @DeleteMapping("/detail/delete/{feedId}")
    public ResponseEntity<String> deleteFeed(@PathVariable Long feedId) {
        feedService.deleteFeedDetail(feedId);

        return ResponseEntity.ok("피드가 성공적으로 삭제되었습니다.");
    }
}

