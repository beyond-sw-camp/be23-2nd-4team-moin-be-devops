package com.team4.moin.manner.repository;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.manner.domain.MannerLog;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.user.domain.entitys.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MannerLogRepository extends JpaRepository<MannerLog, Long> {


    boolean existsByTargetUser_IdAndMeeting_IdAndTypeIn(
            Long targetUserId,
            Long meetingId,
            Collection<String> types
    );
    boolean existsByEvaluator_IdAndTargetUser_IdAndMeeting_IdAndTypeIn(
            Long evaluatorId,
            Long targetUserId,
            Long meetingId,
            Collection<String> types
    );

}
