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
    // 1. 중복 상호 평가 체크 (String 리스트로 변경)
    boolean existsByEvaluatorAndTargetUserAndCrewAndTypeIn(
            User evaluator, User targetUser, Crew crew, List<String> types);

    // 2. 출석 체크 중복 확인 (String 리스트로 변경)
    boolean existsByTargetUserAndCrewAndTypeIn(
            User targetUser, Crew crew, Collection<String> types);

    // 3. 마이페이지용 최신 5건 조회
    List<MannerLog> findTop5ByTargetUserOrderByCreatedTimeDesc(User targetUser);

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
