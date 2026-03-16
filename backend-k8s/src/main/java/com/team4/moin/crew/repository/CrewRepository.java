package com.team4.moin.crew.repository;

import com.team4.moin.crew.domain.entitys.Crew;

import com.team4.moin.user.domain.enums.CategoryType;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CrewRepository extends JpaRepository<Crew, Long> {

    Page<Crew> findAll(Specification<Crew> specification, Pageable pageable);

    Page<Crew> findAllByDelYn(String delYn, Pageable pageable);
    // 찜하기 카운트 증가 (원자적 업데이트)
    @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트 초기화
    @Query("UPDATE Crew c SET c.favoriteCount = c.favoriteCount + 1 WHERE c.id = :id")
    void incrementFavoriteCount(@Param("id") Long id);

    // 찜하기 카운트 감소 (원자적 업데이트, 0 미만 방지)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Crew c SET c.favoriteCount = c.favoriteCount - 1 WHERE c.id = :id AND c.favoriteCount > 0")
    void decrementFavoriteCount(@Param("id") Long id);

    //    해당 크루의 조회수를 +1
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Crew c SET c.viewCount = c.viewCount + 1 WHERE c.id = :crewId")
    int incrementViewCount(@Param("crewId") Long crewId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Crew c where c.id = :crewId")
    Optional<Crew> findByIdForUpdate(@Param("crewId") Long crewId);

    @Query("SELECT c FROM Crew c " +
            "WHERE c.delYn = 'No' " +
            "AND c.currentMemberCount < c.maxMembers " +
            "ORDER BY (" +
            "  (CASE WHEN c.district = :userDistrict THEN 40.0 ELSE 0.0 END) + " +
            "  (CASE WHEN c.categoryType IN :userCategories THEN 30.0 ELSE 0.0 END) + " +
            "  c.ratingAvg + " +
            "  (c.favoriteCount * 0.5) " +
            ") DESC")
    Page<Crew> findRecommendedCrews(
            @Param("userDistrict") String userDistrict,
            @Param("userCategories") List<CategoryType> userCategories,
            Pageable pageable
    );

    @Query("SELECT c FROM Crew c " +
            "WHERE c.delYn = 'No' AND c.currentMemberCount < c.maxMembers " +
            "ORDER BY (c.ratingAvg + (c.favoriteCount * 0.5)) DESC")
    Page<Crew> findPopularCrews(Pageable pageable);
    // 최근 14일 이내 생성된 크루 중 조회수 순으로 페이징 조회
    @Query("SELECT c FROM Crew c " +
            "WHERE c.delYn = 'No' " +
            "AND c.createdTime >= :fourteenDaysAgo " +
            "ORDER BY c.viewCount DESC")
    Page<Crew> findRookieCrews(
            @Param("fourteenDaysAgo") LocalDateTime fourteenDaysAgo,
            Pageable pageable
    );
    @Query("SELECT c FROM Crew c WHERE c.delYn = 'No' AND " +
            "(c.name LIKE CONCAT(:keyword, '%') OR c.chosung LIKE CONCAT(:keyword, '%'))")
    List<Crew> findTop5BySearchKeyword(@Param("keyword") String keyword);
}
