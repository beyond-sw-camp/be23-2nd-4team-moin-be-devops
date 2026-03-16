package com.team4.moin.fee.repository;

import com.team4.moin.fee.domain.SettlementLog;
import com.team4.moin.fee.domain.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SettlementLogRepository extends JpaRepository<SettlementLog, Long> {


    @Query(value = "SELECT s FROM SettlementLog s " +
            "JOIN FETCH s.meeting m " +
            "JOIN FETCH s.user u " +
            "WHERE (:search IS NULL OR m.name LIKE %:search% OR u.email LIKE %:search%)",
            countQuery = "SELECT COUNT(s) FROM SettlementLog s " +
                    "LEFT JOIN s.meeting m " +
                    "LEFT JOIN s.user u " +
                    "WHERE (:search IS NULL OR m.name LIKE %:search% OR u.email LIKE %:search%)")
    Page<SettlementLog> findAllWithSearch(String search, Pageable pageable);
    // 내가 방장인 모든 모임의 '이미 정산된 금액' 총합 구하기
    @Query("SELECT SUM(sl.amount) FROM SettlementLog sl " +
            "JOIN MeetingMember mm ON mm.meeting = sl.meeting " +
            "WHERE mm.crewMember.user.id = :ownerId " +
            "AND mm.role = com.team4.moin.meeting.domain.enums.MeetingRole.OWNER " +
            "AND sl.isSettled = 'YES'")
    Long sumTotalSettledByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT SUM(s.amount) FROM SettlementLog s WHERE s.meeting.id = :meetingId AND s.isSettled IN :statuses")
    Long sumAmountByMeetingIdAndStatusIn(@Param("meetingId") Long meetingId, @Param("statuses") List<SettlementStatus> statuses);

    // 정산 완료 여부 확인
    boolean existsByMeetingIdAndIsSettled(Long meetingId, SettlementStatus status);
    @Query("SELECT SUM(s.amount) FROM SettlementLog s WHERE s.meeting.id = :meetingId AND s.isSettled = :status")
    Long sumAmountByMeetingIdAndStatus(@Param("meetingId") Long meetingId, @Param("status") SettlementStatus status);
    @Query("SELECT COUNT(s) > 0 FROM SettlementLog s " +
            "WHERE s.meeting.id = :meetingId " +
            "AND s.isSettled = :status " +
            "AND s.createdTime > :memberJoinedAt")
    boolean existsByMeetingIdAndIsSettledAndCreatedTimeAfter(
            @Param("meetingId") Long meetingId,
            @Param("status") SettlementStatus status,
            @Param("memberJoinedAt") LocalDateTime memberJoinedAt
    );
}
