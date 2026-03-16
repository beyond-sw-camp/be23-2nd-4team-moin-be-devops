package com.team4.moin.feed.service;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.feed.domain.Feed;
import com.team4.moin.feed.domain.FeedImage;
import com.team4.moin.feed.domain.FeedLike;
import com.team4.moin.feed.dtos.*;
import com.team4.moin.feed.repository.CommentRepository;
import com.team4.moin.feed.repository.FeedLikeRepository;
import com.team4.moin.feed.repository.FeedRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeedService {
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final S3Client s3Client;
    private final FeedLikeRepository feedLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket2}")
    private String bucket;

    @Autowired
    public FeedService(FeedRepository feedRepository, UserRepository userRepository, CrewRepository crewRepository, CrewMemberRepository crewMemberRepository, S3Client s3Client, FeedLikeRepository feedLikeRepository, CommentRepository commentRepository, CommentService commentService, S3Presigner s3Presigner) {
        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.s3Client = s3Client;
        this.feedLikeRepository = feedLikeRepository;
        this.commentRepository = commentRepository;
        this.commentService = commentService;
        this.s3Presigner = s3Presigner;
    }

    public Long createFeed(FeedCreateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 검증 (유저, 크루, 가입상태)
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Crew crew = crewRepository.findById(dto.getCrewId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 크루입니다."));

        boolean isApproved = crewMemberRepository.existsByCrewIdAndUserEmailAndStatus(dto.getCrewId(), email, CrewMemberStatus.APPROVED);

        if (!isApproved) throw new IllegalStateException("승인된 크루 멤버만 작성이 가능합니다.");

        // 이미지 개수 체크 (최대 10장)
        if (dto.getImages() != null && dto.getImages().size() > 10) {
            throw new IllegalArgumentException("사진은 최대 10장까지 업로드 가능합니다.");
        }

        // Feed 저장
        Feed feed = feedRepository.save(dto.toEntity(user, crew));

        //  이미지 S3 업로드 및 FeedImage 연관관계 저장
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                String imgUrl = dto.getImages().get(i);

                FeedImage feedImage = FeedImage.builder()
                        .feed(feed)
                        .imageUrl(imgUrl)
                        .sequence(i) // 프론트에서 보낸 순서대로
                        .build();

                feed.getImages().add(feedImage);
            }
        }

        return feed.getId();
    }

    public void updateFeedContent(FeedTextUpdateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Feed feed = feedRepository.findById(dto.getFeedId())
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다."));

        if (!feed.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 게시글만 수정할 수 있습니다.");
        }

        // 텍스트 업데이트 영속성
        feed.updateContent(dto.getContent());
    }

    public void updateFeedImages(Long feedId, List<String> newImages) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Feed feed = feedRepository.findById(feedId).orElseThrow();

        if (!feed.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        // 새 목록에 포함될 URL 집합 (유지되는 이미지)
        Set<String> keepUrls = newImages != null
                ? new HashSet<>(newImages)
                : Set.of();

        // S3에서는 "제거되는" 이미지만 삭제 (새 목록에 없는 기존 이미지)
        if (feed.getImages() != null && !feed.getImages().isEmpty()) {
            for (FeedImage img : feed.getImages()) {
                String url = img.getImageUrl();
                if (url == null || keepUrls.contains(url)) continue; // 유지되는 건 삭제 안 함
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
            feed.getImages().clear();
        }

        //  새 이미지 등록
        if (newImages != null && !newImages.isEmpty()) {
            for (int i = 0; i < newImages.size(); i++) {
                String imgUrl = newImages.get(i);
                    feed.getImages().add(FeedImage.builder()
                            .feed(feed)
                            .imageUrl(imgUrl)
                            .sequence(i)
                            .build());
            }
        }
    }

    public void deleteFeedImage(Long feedId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다."));

        if (!feed.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 게시글만 삭제할 수 있습니다.");
        }

        if (!feed.getImages().isEmpty()) {
            for (FeedImage img : feed.getImages()) {
                String url = img.getImageUrl();
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
        }

        // FeedImage에 CascadeType.ALL과 orphanRemoval=true가 걸어 연관된 이미지 레코드도 자동 삭제.
        feedRepository.delete(feed);
    }

    // 피드 좋아요
    public void addLike(Long feedId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        if (!feedRepository.existsById(feedId)) {
            throw new EntityNotFoundException("피드를 찾을 수 없습니다.");
        }
        // DB에 좋아요 저장
        feedLikeRepository.insertIgnore(feedId, userId);
    }
//피드 좋아요 취소
    public void removeLike(Long feedId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        // DB에서 삭제
        feedLikeRepository.deleteByFeedIdAndUserId(feedId, userId);
    }
    @Transactional(readOnly = true)
    public Page<FeedResponseDto> getFeeds(Long crewId, Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 피드 목록 조회 ( EntityGraph 덕분에 User는 한 번에 가져오고, Images는 BatchSize로 처리됨)
        Page<Feed> feedPage = (crewId != null)
                ? feedRepository.findAllByCrewIdAndUser_DelYnOrderByCreatedTimeDesc(crewId, "No", pageable)
                : feedRepository.findAllByUser_DelYnOrderByCreatedTimeDesc("No", pageable);

        if (feedPage.isEmpty()) return Page.empty(pageable); // 널포인트 방지 빈 객체 반환

        //  현재 페이지 피드 ID 리스트 추출
        List<Long> feedIds = feedPage.getContent().stream()
                .map(Feed::getId)
                .collect(Collectors.toList());

        //  좋아요 개수 한꺼번에 가져와서 Map으로 변환
        Map<Long, Long> likeCounts = feedLikeRepository.countLikesByFeedIds(feedIds)
                .stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (Long) obj[1]));

        //  댓글 개수 한꺼번에 가져와서 Map으로 변환
        Map<Long, Long> commentCounts = commentRepository.countCommentsByFeedIds(feedIds)
                .stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (Long) obj[1]));

        //  내가 좋아요 누른 피드 ID들 Set으로 변환 (O(1) 검색을 위함)
        Set<Long> myLikedIdSet = new HashSet<>(feedLikeRepository.findMyLikedFeedIds(feedIds, email));

        return feedPage.map(feed -> {
            List<String> imageUrls = feed.getImages().stream()
                    .sorted(Comparator.comparingInt(FeedImage::getSequence))
                    .map(FeedImage::getImageUrl)
                    .collect(Collectors.toList());

            return FeedResponseDto.of(
                    feed,
                    imageUrls,
                    likeCounts.getOrDefault(feed.getId(), 0L),
                    commentCounts.getOrDefault(feed.getId(), 0L),
                    myLikedIdSet.contains(feed.getId()),
                    feed.getUser().getEmail().equals(email)
            );
        });
    }

    @Transactional(readOnly = true)
    public FeedDetailResponseDto getFeedDetail(Long feedId, Pageable commentPageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Feed feed = feedRepository.findByIdWithImagesAndUser(feedId)
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다."));

        List<String> imageUrls = feed.getImages().stream()
                .map(FeedImage::getImageUrl)
                .collect(Collectors.toList());

        //  좋아요 정보 조회
        long likeCount = feedLikeRepository.countByFeedIdAndUser_DelYn(feedId, "No");
        boolean isLiked = feedLikeRepository.existsByFeedIdAndUserEmail(feedId, email);

        boolean isMyPost = feed.getUser().getEmail().equals(email);

        //   댓글 조회 서비스 호출
        Page<CommentResponseDto> commentPage = commentService.getComments(feedId, commentPageable);

        return FeedDetailResponseDto.fromEntity(feed, imageUrls, likeCount, isLiked, isMyPost, commentPage);
    }
    public void deleteFeedDetail(Long feedId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다."));

        if (!feed.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 게시글만 삭제할 수 있습니다.");
        }

        if (feed.getImages() != null && !feed.getImages().isEmpty()) {
            for (FeedImage img : feed.getImages()) {
                String url = img.getImageUrl();
                String fileName = url.substring(url.lastIndexOf("/") + 1);

                s3Client.deleteObject(a -> a.bucket(bucket).key(fileName));
            }
        }

        feedRepository.delete(feed);
    }

    // URL 발급 메서드
    public String getPresignedUrl(String originalFileName) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = "feeds-" + user.getId() + "_" + System.currentTimeMillis() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // URL 유효시간 5분
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
    }

