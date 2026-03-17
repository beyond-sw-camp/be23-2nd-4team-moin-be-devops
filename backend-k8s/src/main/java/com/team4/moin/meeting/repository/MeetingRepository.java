package com.team4.moin.meeting.repository;

import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingFeeType;
import com.team4.moin.meeting.domain.enums.MeetingRecruitStatus;
import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    //  크루별 모임 목록
    Page<Meeting> findAllByCrew_Id(Long crewId, Pageable pageable);



    // 크루 안의 특정 모임 상세 조회(잠금 없음)
    Optional<Meeting> findByIdAndCrew_Id(Long meetingId, Long crewId);

    // 비관적락 적용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Meeting m where m.id = :meetingId and m.crew.id = :crewId")
    Optional<Meeting> findByIdAndCrew_IdForUpdate(Long meetingId, Long crewId);


    @Query("SELECT SUM(m.totalFeePool) FROM Meeting m " +
            "JOIN MeetingMember mm ON mm.meeting = m " +
            "WHERE mm.crewMember.user.id = :ownerId " +
            "AND mm.role = com.team4.moin.meeting.domain.enums.MeetingRole.OWNER " +
            "AND m.feeType = com.team4.moin.meeting.domain.enums.MeetingFeeType.PAID")
    Long sumTotalFeePoolByOwnerId(@Param("ownerId") Long ownerId);



    @Query("SELECT DISTINCT m FROM MeetingMember mm " +
            "JOIN mm.meeting m " +
            "WHERE mm.crewMember.user.id = :userId " +
            "AND mm.role = :role " +           // 하드코딩 대신 파라미터 사용
            "AND m.feeType = :feeType")        // 하드코딩 대신 파라미터 사용
    List<Meeting> findAllPaidMeetingsByOwnerId(@Param("userId") Long userId, @Param("role") MeetingRole role, @Param("feeType") MeetingFeeType feeType);

    // [추가된 부분] 정산 대상 모임 조회 (미정산 && 유료 && 모임 시작 3시간 이내)
    @Query("SELECT m FROM Meeting m WHERE m.feeType = 'PAID' AND m.isSettled = false AND m.meetingAt <= :targetTime")
    List<Meeting> findMeetingsToSettle(@Param("targetTime") LocalDateTime targetTime);
    // 1. 특정 크루 내에서의 마감 임박 모임
    @Query("SELECT m FROM Meeting m " +
            "WHERE m.crew.id = :crewId " +
            "AND m.recruitStatus = 'OPEN' " +
            "AND m.meetingAt > CURRENT_TIMESTAMP " + // 아직 시작 안 한 모임만
            "AND m.currentMemberCount < m.maxMembers " + // 정원이 다 차지 않은 모임만
            "AND ( (m.maxMembers - m.currentMemberCount) <= 2 OR m.meetingAt <= :deadline ) " + // 남은 자리 2개 이하 OR 마감 임박
            "ORDER BY m.meetingAt ASC") // 날짜가 가까운 순으로
    List<Meeting> findImminentMeetingsByCrew(
            @Param("crewId") Long crewId,
            @Param("deadline") LocalDateTime deadline,
            Pageable pageable
    );
    /**
     * 유저 ID와 승인 상태를 조건으로, 해당 유저가 참여 중인 모임(Meeting) 목록만 직접 조회
     */
    @Query("SELECT mm.meeting FROM MeetingMember mm " +
            "WHERE mm.crewMember.user.id = :userId " +
            "AND mm.status = :status " +
            "ORDER BY mm.meeting.meetingAt ASC")
    List<Meeting> findMeetingsByUserIdAndStatusWithinOneWeek(
            @Param("userId") Long userId,
            @Param("status") MeetingMemberStatus status,
            @Param("now") LocalDateTime now,
            @Param("oneWeekLater") LocalDateTime oneWeekLater
    );


    @Query("SELECT mm.meeting FROM MeetingMember mm " +
            "WHERE mm.crewMember.user.id = :userId " +
            "AND mm.status = :status " +
            "AND mm.meeting.recruitStatus = 'FINISHED' " +
            "AND mm.meeting.crew.delYn != 'Yes'") // ORDER BY 절 삭제
    Page<Meeting> findFinishedMeetingsByUserId(
            @Param("userId") Long userId,
            @Param("status") MeetingMemberStatus status,
            Pageable pageable);
}
