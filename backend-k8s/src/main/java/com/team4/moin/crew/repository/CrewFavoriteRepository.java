package com.team4.moin.crew.repository;

import com.team4.moin.crew.domain.entitys.CrewFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewFavoriteRepository extends JpaRepository<CrewFavorite, Long> {


//        크루 삭제시 찜 전체삭제
    void deleteAllByCrew_Id(Long crewId);
//    // 특정 유저가 찜한 목록을 가져오되, 크루 정보까지 한 번에 패치조인
    @Query("SELECT cf FROM CrewFavorite cf JOIN FETCH cf.crew WHERE cf.user.id = :userId")
    List<CrewFavorite> findAllByUserIdWithCrew(@Param("userId") Long userId);
    @Query("SELECT COUNT(cf) FROM CrewFavorite cf " +
            "WHERE cf.user.id = :userId AND cf.crew.delYn = :delYn")
    long countActiveFavoritesByUserId(@Param("userId") Long userId,
                                      @Param("delYn") String delYn);

    Page<CrewFavorite> findByUser_IdAndCrew_DelYn(Long userId, String delYn, Pageable pageable);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CrewFavorite cf WHERE cf.crew.id = :crewId AND cf.user.id = :userId")
    int deleteByCrewIdAndUserId(@Param("crewId") Long crewId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    INSERT IGNORE INTO crew_favorite (crew_id, user_id, created_time, updated_time)
    VALUES (:crewId, :userId, NOW(), NOW())
""", nativeQuery = true)
    int insertIgnore(@Param("crewId") Long crewId, @Param("userId") Long userId);
}