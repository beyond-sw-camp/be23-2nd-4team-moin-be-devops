package com.team4.moin.feed.service;

import com.team4.moin.feed.domain.Comment;
import com.team4.moin.feed.domain.CommentLike;
import com.team4.moin.feed.domain.Feed;
import com.team4.moin.feed.dtos.CommentCreateDto;
import com.team4.moin.feed.dtos.CommentResponseDto;
import com.team4.moin.feed.repository.CommentLikeRepository;
import com.team4.moin.feed.repository.CommentRepository;
import com.team4.moin.feed.repository.FeedRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, FeedRepository feedRepository, UserRepository userRepository, CommentLikeRepository commentLikeRepository) {
        this.commentRepository = commentRepository;
        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    //  댓글 작성
    public CommentResponseDto createComment(CommentCreateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Feed feed = feedRepository.findById(dto.getFeedId())
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다."));

        Comment parent = null;
        // 대댓글이 있으면 부모 댓글을 찾아 대댓글로 설정
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));

            //  대댓글의 대댓글 방지
            if (parent.getParent() != null) {
                throw new IllegalStateException("대댓글에는 답글을 달 수 없습니다.");
            }
        }

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .feed(feed)
                .user(user)
                .parent(parent) // 대댓글이면 parent 할당, 아니면 null
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentResponseDto.builder()
                .commentId(saved.getId())
                .content(saved.getContent())
                .nickname(user.getNickname())              // 이미 로드된 user 사용
                .profileImageUrl(user.getProfileImageUrl()) // 이미 로드된 user 사용
                .createdTime(saved.getCreatedTime())
                .likeCount(0)
                .isLiked(false)
                .isMyComment(true)
                .parentId(parent != null ? parent.getId() : null)
                .reply(null)
                .build();
    }


    //  댓글 삭제
    public void deleteComment(Long commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new EntityNotFoundException("없는 댓글입니다."));

        if (!comment.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }

    //  댓글 조회 (과거순 정렬)
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(Long feedId, Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Page<Comment> parents = commentRepository.findParentsWithUser(feedId, "No", pageable);
        if (parents.isEmpty()) return Page.empty(pageable);

        List<Long> allIds = parents.getContent().stream()
                .flatMap(p -> Stream.concat(Stream.of(p.getId()), p.getChildren().stream().map(Comment::getId)))
                .collect(Collectors.toList());

        Map<Long, Long> likeCounts = commentLikeRepository.countLikesByCommentIds(allIds)
                .stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (Long) obj[1]));

        Set<Long> myLikedIdSet = new HashSet<>(commentLikeRepository.findMyLikedCommentIds(allIds, email));

        return parents.map(parent -> CommentResponseDto.fromEntity(
                parent,
                likeCounts.getOrDefault(parent.getId(), 0L),
                myLikedIdSet.contains(parent.getId()),
                parent.getUser().getEmail().equals(email),
                convertToReplyDto(parent, email, likeCounts, myLikedIdSet)
        ));
    }

    // 대댓글 조립용  메서드
    private CommentResponseDto convertToReplyDto(Comment parent, String email, Map<Long, Long> likeCounts, Set<Long> myLikedIdSet) {
        if (parent.getChildren().isEmpty()) return null;

        Comment child = parent.getChildren().get(0);
        if ("Yes".equals(child.getUser().getDelYn())) return null;

        return CommentResponseDto.fromEntity(
                child,
                likeCounts.getOrDefault(child.getId(), 0L),
                myLikedIdSet.contains(child.getId()),
                child.getUser().getEmail().equals(email),
                null
        );
    }

//   댓글 좋아요
    public void addCommentLike(Long commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("댓글을 찾을 수 없습니다.");
        }
        // DB에 좋아요 추가
        commentLikeRepository.insertIgnore(commentId, userId);
    }

//    댓글 좋아요 ㅊ취소
    public void removeCommentLike(Long commentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        // DB에서 삭제
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
    }
}