package com.team4.moin.fee.repository;

import com.team4.moin.fee.domain.RefundLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundLogRepository extends JpaRepository<RefundLog, Long> {
    @Query("SELECT r FROM RefundLog r WHERE r.meeting.id = :meetingId")
    List<RefundLog> findAllByMeetingId(@Param("meetingId") Long meetingId);
}
