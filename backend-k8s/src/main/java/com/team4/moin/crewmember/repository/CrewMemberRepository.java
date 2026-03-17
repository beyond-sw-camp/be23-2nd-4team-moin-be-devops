package com.team4.moin.crewmember.repository;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.chat.domain.ChatRoom;
import com.team4.moin.user.domain.entitys.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {

    Optional<CrewMember> findByCrewAndUser(Crew crew, User user);

//    user email로 CrewMember 다건 조회
    @Query("SELECT cm FROM CrewMember cm JOIN FETCH cm.user u JOIN FETCH cm.crew c WHERE u.email = :email")
    List<CrewMember> findAllByUserEmail(@Param("email") String email);

//    크루의 가입상태별 인원 수 count
    Integer countByCrew_IdAndStatus(Long crewId, CrewMemberStatus crewMemberStatus);
//   크루 중복 가입신청 방지 (exists : 조건에 맞는 데이터가 하나라도 있는지 확인)
    boolean existsByCrew_IdAndUser_IdAndStatusIn(Long crewId, Long userId, List<CrewMemberStatus> statuses);

//   크루 권한 확인
    Optional<CrewMember> findByCrew_IdAndUser_IdAndStatus(Long crewId, Long userId, CrewMemberStatus status);


//    이 크루에 신청한 가입이 맞는지 확인
    Optional<CrewMember> findByIdAndCrew_Id(Long id, Long crewId);




    //  유저 ID, 상태, 그리고 크루의 delYn이 일치하는 개수
    long countByUser_IdAndStatusAndCrew_DelYn(Long userId, CrewMemberStatus status, String delYn);

    //  유저 ID, 역할, 그리고 크루의 delYn이 일치하는 개수
    long countByUser_IdAndRoleAndCrew_DelYn(Long userId, CrewRole role, String delYn);
    boolean existsByCrewIdAndUserEmailAndStatus(Long crewId, String email, CrewMemberStatus status);

    Optional<CrewMember> findByCrew_IdAndUser_IdAndStatusIn(Long crewId, Long userId, List<CrewMemberStatus> statuses);

    // CrewMember + User + Categories
    @Query("SELECT cm FROM CrewMember cm JOIN FETCH cm.user u LEFT JOIN FETCH u.categories WHERE cm.id = :crewMemberId AND cm.crew.id = :crewId")
    Optional<CrewMember> findByIdAndCrew_IdWithUserAndCategories(@Param("crewMemberId") Long crewMemberId,
                                                                 @Param("crewId") Long crewId);

    // 대상 유저의 가입 크루 목록
    @Query("SELECT cm FROM CrewMember cm JOIN FETCH cm.crew c WHERE cm.user.id = :userId " +
            "AND cm.status = 'APPROVED' AND c.delYn = 'No'")
    List<CrewMember> findApprovedCrewsByUserId(@Param("userId") Long userId);
    // 참여 대기 목록용
    Page<CrewMember> findByUser_IdAndStatusAndCrew_DelYn(Long userId, CrewMemberStatus status, String delYn, Pageable pageable);

    // 참여 중 목록용 (OWNER 제외)
    Page<CrewMember> findByUser_IdAndStatusAndRoleNotAndCrew_DelYn(Long userId, CrewMemberStatus status, CrewRole role, String delYn, Pageable pageable);

    // 운영 중 목록용 (OWNER 만)
    Page<CrewMember> findByUser_IdAndStatusAndRoleAndCrew_DelYn(Long userId, CrewMemberStatus status, CrewRole role, String delYn, Pageable pageable);

    Optional<CrewMember> findByCrew_IdAndRole(Long crewId, CrewRole role);

    // 가입신청 목록 조회용: user를 같이 fetch해서 DTO 변환 시 N+1 방지
    @Query("""
    SELECT cm
    FROM CrewMember cm
    JOIN FETCH cm.user u
    WHERE cm.crew.id = :crewId
      AND cm.status = :status
""")
    List<CrewMember> findAllByCrewIdAndStatusWithUser(
            @Param("crewId") Long crewId,
            @Param("status") CrewMemberStatus status
    );

}
