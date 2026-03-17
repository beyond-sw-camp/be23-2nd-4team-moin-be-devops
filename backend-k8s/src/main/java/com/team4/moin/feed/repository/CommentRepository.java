package com.team4.moin.feed.repository;

import com.team4.moin.feed.domain.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    //  피드 ID 리스트를 받아 댓글 개수를 한 번에 조회
    @Query("SELECT c.feed.id, COUNT(c) FROM Comment c WHERE c.feed.id IN :feedIds AND c.user.delYn = 'No' GROUP BY c.feed.id")
    List<Object[]> countCommentsByFeedIds(@org.springframework.data.repository.query.Param("feedIds") List<Long> feedIds);

    @Query(value = "select c from Comment c " +
            "join fetch c.user " +
            "where c.feed.id = :feedId and c.parent is null and c.user.delYn = :delYn",
            countQuery = "select count(c) from Comment c where c.feed.id = :feedId and c.parent is null")
    Page<Comment> findParentsWithUser(@Param("feedId") Long feedId, @Param("delYn") String delYn, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id IN :ids")
    List<Comment> findAllByIdWithUser(@Param("ids") Collection<Long> ids);
}
