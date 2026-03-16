package com.team4.moin.feed.repository;

import com.team4.moin.feed.domain.Feed;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    //  @EntityGraph를 사용하여 Join을 통해 유저 정보와 이미지 정보를 한 번에 가져오기
//    id 기준 내림차순으로
    @EntityGraph(attributePaths = {"user"})
    Page<Feed> findAllByCrewIdAndUser_DelYnOrderByCreatedTimeDesc(Long crewId,String delYn ,Pageable pageable);
     @EntityGraph(attributePaths = {"user",})
     Page<Feed> findAllByUser_DelYnOrderByCreatedTimeDesc(String delYn, Pageable pageable);
    @Query("select f from Feed f join fetch f.user where f.id = :feedId")
    Optional<Feed> findByIdWithImagesAndUser(@Param("feedId") Long feedId);

    long deleteByCrew_Id(Long crewId);
    long deleteByCrew_IdAndUser_Id(Long crewId, Long userId);


}
