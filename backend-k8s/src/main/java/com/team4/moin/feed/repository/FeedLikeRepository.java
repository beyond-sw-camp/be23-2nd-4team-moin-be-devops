package com.team4.moin.feed.repository;

import com.team4.moin.feed.domain.FeedLike;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    //  상세 페이지용
    long countByFeedIdAndUser_DelYn(Long feedId, String delYn);
    boolean existsByFeedIdAndUserEmail(Long feedId, String email);

    //  목록 조회 최적화용 (IN 쿼리)
    @Query("SELECT fl.feed.id, COUNT(fl) FROM FeedLike fl WHERE fl.feed.id IN :feedIds GROUP BY fl.feed.id")
    List<Object[]> countLikesByFeedIds(@Param("feedIds") List<Long> feedIds);

    @Query("SELECT fl.feed.id FROM FeedLike fl WHERE fl.feed.id IN :feedIds AND fl.user.email = :email")
    List<Long> findMyLikedFeedIds(@Param("feedIds") List<Long> feedIds, @Param("email") String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    INSERT IGNORE INTO feed_like (feed_id, user_id, created_time, updated_time)
    VALUES (:feedId, :userId, NOW(), NOW())
""", nativeQuery = true)
    int insertIgnore(@Param("feedId") Long feedId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FeedLike fl WHERE fl.feed.id = :feedId AND fl.user.id = :userId")
    int deleteByFeedIdAndUserId(@Param("feedId") Long feedId, @Param("userId") Long userId);
}
