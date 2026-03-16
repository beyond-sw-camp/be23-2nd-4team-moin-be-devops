package com.team4.moin.feed.repository;

import com.team4.moin.feed.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    // 좋아요 토글을 위한 조회
    Optional<CommentLike> findByCommentIdAndUserEmail(Long commentId, String email);

    //  댓글 ID 리스트를 받아 각 댓글당 좋아요 개수를 한 번에 집계
    @Query("SELECT cl.comment.id, COUNT(cl) FROM CommentLike cl " +
            "WHERE cl.comment.id IN :commentIds GROUP BY cl.comment.id")
    List<Object[]> countLikesByCommentIds(@Param("commentIds") List<Long> commentIds);

    // 내가 좋아요를 누른 댓글 ID들만 한 번에 추출
    @Query("SELECT cl.comment.id FROM CommentLike cl " +
            "WHERE cl.comment.id IN :commentIds AND cl.user.email = :email")
    List<Long> findMyLikedCommentIds(@Param("commentIds") List<Long> commentIds, @Param("email") String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    int deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    INSERT IGNORE INTO comment_like (comment_id, user_id)
    VALUES (:commentId, :userId)
""", nativeQuery = true)
    int insertIgnore(@Param("commentId") Long commentId, @Param("userId") Long userId);

}
