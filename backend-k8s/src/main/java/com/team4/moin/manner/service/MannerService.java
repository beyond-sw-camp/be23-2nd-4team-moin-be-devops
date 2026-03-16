package com.team4.moin.manner.service;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.manner.domain.MannerLog;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.EvaluationType;
import com.team4.moin.user.domain.enums.GoodBad;
import com.team4.moin.manner.dtos.manner.AttendanceRequestDto;
import com.team4.moin.manner.dtos.manner.EvaluationRequestDto;
import com.team4.moin.manner.repository.MannerLogRepository;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class MannerService {

    private final MannerLogRepository mannerLogRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;

    public MannerService(MannerLogRepository mannerLogRepository, CrewMemberRepository crewMemberRepository, UserRepository userRepository, MeetingRepository meetingRepository) {
        this.mannerLogRepository = mannerLogRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
    }
    // 1. 멤버 상호 평가 로직 (GoodBad 사용)
    public BigDecimal evaluateMember(EvaluationRequestDto dto) {
        // 평가자(나) 조회
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 피평가자(상대방) 조회
        User target = userRepository.findByIdWithLock(dto.getTargetUserId())
                .orElseThrow(() -> new EntityNotFoundException("평가 대상을 찾을 수 없습니다."));
        Meeting meeting = meetingRepository.findById(dto.getMeetingId())
                .orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        // 자격 검증: 동일 모임 멤버인지 확인
        if (mannerLogRepository.existsByEvaluator_IdAndTargetUser_IdAndMeeting_IdAndTypeIn(
                me.getId(), target.getId(), dto.getMeetingId(), List.of("GOOD", "BAD"))) {
            throw new IllegalStateException("이미 이 모임에서 해당 멤버 평가를 완료했습니다.");
        }

        // 중복 평가 체크 (이미 GOOD/BAD 평가를 했는지)
        if (mannerLogRepository.existsByEvaluator_IdAndTargetUser_IdAndMeeting_IdAndTypeIn(
                me.getId(), target.getId(), dto.getMeetingId(), List.of("GOOD", "BAD"))) {
            throw new IllegalStateException("이미 이 모임에서 해당 멤버 평가를 완료했습니다.");
        }

        // 점수 결정: GOOD(+0.2), BAD(-0.3)
        BigDecimal delta = (dto.getEvaluation() == GoodBad.GOOD)
                ? new BigDecimal("0.2")
                : new BigDecimal("-0.3");

        // 점수 반영
        target.updateMannerScore(delta);

        // 로그 저장
        mannerLogRepository.save(MannerLog.builder()
                .evaluator(me)
                .targetUser(target)
                .crew(Crew.builder().id(dto.getCrewId()).build())
                .meeting(meeting)
                .type(dto.getEvaluation().name())
                .scoreDelta(delta)
                .build());

        return target.getMannerScore();
    }


//      2. 출석 체크 로직

    public BigDecimal processAttendanceScore(AttendanceRequestDto dto) {
        // 피평가자(대상 유저) 조회
        User target = userRepository.findByIdWithLock(dto.getTargetUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Meeting meeting = meetingRepository.findById(dto.getMeetingId())
                .orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        // 중복 처리 검증 (이미 해당 크루에서 출석/노쇼 로그가 있는지 확인)
        if (mannerLogRepository.existsByTargetUser_IdAndMeeting_IdAndTypeIn(
                dto.getTargetUserId(),
                dto.getMeetingId(),
                List.of("ATTEND", "NO_SHOW"))) {
            throw new IllegalStateException("이미 출석/노쇼 처리가 완료된 멤버입니다.");
        }

        // 점수 가중치 결정 (참여 +0.2, 노쇼 -0.5)
        BigDecimal delta = (dto.getType() == EvaluationType.ATTEND)
                ? new BigDecimal("0.2")
                : new BigDecimal("-0.5");

        // 유저 점수 업데이트
        target.updateMannerScore(delta);

        // 매너 로그 저장
        mannerLogRepository.save(MannerLog.builder()
                .targetUser(target)
                .crew(Crew.builder().id(dto.getCrewId()).build())
                .meeting(meeting)
                .type(dto.getType().name())
                .scoreDelta(delta)
                .build());

        return target.getMannerScore();
    }
}
