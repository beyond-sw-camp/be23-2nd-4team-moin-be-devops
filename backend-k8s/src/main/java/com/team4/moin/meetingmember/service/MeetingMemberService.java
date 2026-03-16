package com.team4.moin.meetingmember.service;

import com.team4.moin.Notification.domain.NotificationType;
import com.team4.moin.Notification.service.NotificationService;
import com.team4.moin.common.service.PortOneService;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crewmember.dtos.CrewMemberJoinedCrewDto;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.fee.domain.FeeLog;
import com.team4.moin.fee.domain.FeeStatus;
import com.team4.moin.manner.dtos.manner.EvaluationRequestDto;
import com.team4.moin.manner.service.MannerService;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingFeeType;
import com.team4.moin.meeting.domain.enums.MeetingRecruitStatus;
import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import com.team4.moin.meetingmember.dtos.*;
import com.team4.moin.fee.repository.FeeLogRepository;
import com.team4.moin.meetingmember.repository.MeetingMemberRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.GoodBad;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
@Transactional
public class MeetingMemberService {
    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final PortOneService portOneService;
    private final FeeLogRepository feeLogRepository;
    private final MannerService mannerService;
    private final NotificationService notificationService;

    @Autowired
    public MeetingMemberService(MeetingRepository meetingRepository, MeetingMemberRepository meetingMemberRepository, CrewRepository crewRepository, CrewMemberRepository crewMemberRepository, UserRepository userRepository, @Qualifier("paymentInventory") RedisTemplate<String, String> redisTemplate, PortOneService portOneService, FeeLogRepository feeLogRepository, MannerService mannerService, NotificationService notificationService) {
        this.meetingRepository = meetingRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.portOneService = portOneService;
        this.feeLogRepository = feeLogRepository;
        this.mannerService = mannerService;
        this.notificationService = notificationService;
    }

    public MeetingJoinResDto meetingJoin(MeetingMemberReqDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.findByIdAndCrew_IdForUpdate(dto.getMeetingId(), dto.getCrewId())
                .orElseThrow(() -> new EntityNotFoundException("없는 모임 입니다."));

        // 중복 가입 체크 (PENDING, APPROVED 상태인 경우 튕기기)
        if (meetingMemberRepository.existsByMeeting_IdAndCrewMember_User_IdAndStatusIn(
                dto.getMeetingId(), user.getId(), List.of(MeetingMemberStatus.PENDING, MeetingMemberStatus.APPROVED))) {
            throw new IllegalStateException("이미 가입 신청했거나 가입된 모임 입니다.");
        }

        CrewMember crewMember = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(dto.getCrewId(), user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("크루원만 참여 신청 가능합니다."));

        String merchantUid = meeting.createMerchantUid(user.getId());
        MeetingMember join = MeetingMember.builder()
                .meeting(meeting)
                .crewMember(crewMember)
                .status(MeetingMemberStatus.PENDING)
                .merchantUid(merchantUid)
                .joinMessage(dto.getJoinMessage())
                .build();

        boolean approved = false;

        // 유료/무료 분기
        if (!meeting.isPaid()) {
            join.updateStatus(MeetingMemberStatus.APPROVED);
            join.updateRole(MeetingRole.MEMBER);
            meeting.addMemberCount();
            approved = true;

            meetingMemberRepository.findOwnerByMeetingId(meeting.getId()).ifPresent(owner ->
                    notificationService.send(
                            owner,
                            NotificationType.APPLY,
                            "meeting",
                            meeting.getId(),
                            user.getNickname() + "님이 " + meeting.getName() + " 모임에 참여했습니다."
                    )
            );
        } else {
            // 유료일 경우 Redis에 금액 기록
            redisTemplate.opsForValue().set(
                    merchantUid,
                    String.valueOf(meeting.getFee()),
                    Duration.ofMinutes(30)
            );
        }

        MeetingMember saved = meetingMemberRepository.save(join);
        return MeetingJoinResDto.fromEntity(saved, approved);
    }


    public void cancelPayment(String merchantUid) {
        MeetingMember join = meetingMemberRepository.findByMerchantUidWithUser(merchantUid)
                .orElseThrow(() -> new EntityNotFoundException("신청 내역이 없습니다."));

        // 아직 대기중(PENDING)일 때만 삭제 처리
        if (join.getStatus() == MeetingMemberStatus.PENDING) {
            meetingMemberRepository.delete(join);
            redisTemplate.delete(merchantUid);
            log.info("결제 취소로 인한 신청 데이터 삭제 완료: {}", merchantUid);
        }
    }

    @Transactional
    public void paymentSuccess(MeetingPaymentSuccessDto dto) {
        //  Redis에서 결제 예정 금액 확인
        String expectedAmountStr = redisTemplate.opsForValue().get(dto.getMerchantUid());
        if (expectedAmountStr == null) {
            throw new IllegalStateException("결제 유효 시간이 만료되었습니다.");
        }

        //  포트원 V2 실결제 금액 조회
        Long actualAmount = portOneService.getActualPaymentAmount(dto.getImpUid());

        //  금액 위변조 검증
        if (!Long.valueOf(expectedAmountStr).equals(actualAmount)) {
            log.error("금액 위변조 의심: 주문번호 {}, 예상 {}, 실제 {}", dto.getMerchantUid(), expectedAmountStr, actualAmount);
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        //  비관적 락으로 모임 정보 조회 (정원 및 회비 업데이트용)
        Meeting meeting = meetingRepository.findByIdAndCrew_IdForUpdate(dto.getMeetingId(), dto.getCrewId())
                .orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다."));

        //  신청 내역 조회
        MeetingMember join = meetingMemberRepository.findByMerchantUid(dto.getMerchantUid())
                .orElseThrow(() -> new EntityNotFoundException("참여 신청 정보를 찾을 수 없습니다."));

        // 정원 최종 확인
        if (meeting.isFull()) {

            throw new IllegalStateException("정원이 가득 찼습니다.");
        }

        //  회비 적립 및 장부 기록
        meeting.addFee(actualAmount); // 모임 총 회비 증가
        meeting.addMemberCount();     // 현재 참여 인원 증가

        feeLogRepository.save(FeeLog.builder()
                .meetingId(meeting)
                .email(join.getCrewMember().getUser().getEmail())
                .amount(actualAmount)
                .status(FeeStatus.DEPOSIT)
                .build());

        //  가입 상태 완료 처리 (APPROVED)
        join.completePayment(dto.getImpUid());

        //  검증 완료 후 Redis 데이터 삭제
        redisTemplate.delete(dto.getMerchantUid());
        meetingMemberRepository.findOwnerByMeetingId(meeting.getId()).ifPresent(owner ->
                notificationService.send(
                        owner,
                        NotificationType.APPLY,
                        "meeting",
                        meeting.getId(),
                        join.getCrewMember().getUser().getNickname() + "님이 " + meeting.getName() + " 모임에 참여했습니다."
                )
        );
        log.info("회비 적립 완료: 모임 ID {}, 납부자 {}, 금액 {}", meeting.getId(), join.getCrewMember().getUser().getNickname(), actualAmount);
    }

    //    모임원 목록 조회
    public List<MeetingMemberListDto> findMeetingMembers(Long crewId, Long meetingId) {
        //  이 모임(meetingId)가 이 크루(crewId) 소속인지 확인
        meetingRepository.findByIdAndCrew_Id(meetingId, crewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 크루의 모임이 아니거나 존재하지 않는 모임입니다."));
        List<MeetingMember> members = meetingMemberRepository.findByMeetingIdAndStatusWithFetch(meetingId, MeetingMemberStatus.APPROVED);


        List<MeetingMemberListDto> meetingMembers = new ArrayList<>();

        for (MeetingMember member : members) {
            meetingMembers.add(MeetingMemberListDto.fromEntity(member));
        }
        return meetingMembers;
    }

    //모임원들끼리 매너점수 평가
    public BigDecimal evaluateMeetingMember(MeetingEvaluationRequestDto dto) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User evaluator = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        User target = userRepository.findById(dto.getTargetUserId())
                .orElseThrow(() -> new EntityNotFoundException("평가 대상 유저를 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.findByIdAndCrew_Id(dto.getMeetingId(), dto.getCrewId())
                .orElseThrow(() -> new EntityNotFoundException("해당 크루의 모임이 아니거나 존재하지 않는 모임입니다."));

        if (meeting.getRecruitStatus() != MeetingRecruitStatus.FINISHED) {
            throw new IllegalStateException("모임 종료 후에만 평가할 수 있습니다.");
        }
        meetingMemberRepository.findByMeeting_IdAndCrewMember_User_IdAndStatus(dto.getMeetingId(), evaluator.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("이 모임에 참여한 사람만 평가할 수 있습니다."));

        meetingMemberRepository.findByMeeting_IdAndCrewMember_User_IdAndStatus(dto.getMeetingId(), target.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("대상은 이 모임 참여자가 아닙니다."));

        //  자기 자신 평가 X
        if (evaluator.getId().equals(target.getId())) {
            throw new IllegalArgumentException("본인은 평가할 수 없습니다.");
        }
        if (!"GOOD".equals(dto.getType()) && !"BAD".equals(dto.getType())) {
            throw new IllegalArgumentException("type은 GOOD 또는 BAD만 가능합니다.");
        }

        EvaluationRequestDto mannerDto = EvaluationRequestDto.builder()
                .crewId(meeting.getCrew().getId())
                .targetUserId(target.getId())
                .meetingId(dto.getMeetingId())
                .evaluation(GoodBad.valueOf(dto.getType()))
                .build();

        return mannerService.evaluateMember(mannerDto);
    }

    // 모임 권한 변경
    public void changeMeetingRole(MeetingRoleChangeDto dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        // 모임 락 (권한 변경 동시성 방지)
        Meeting meeting = meetingRepository.findByIdAndCrew_IdForUpdate(dto.getMeetingId(), dto.getCrewId())
                .orElseThrow(() -> new EntityNotFoundException("없는 모임 입니다."));

        MeetingMember owner = meetingMemberRepository.findByMeeting_IdAndCrewMember_User_IdAndStatus(dto.getMeetingId(), user.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("해당 모임 멤버가 아닙니다."));

        if (owner.getRole() != MeetingRole.OWNER) {
            throw new IllegalStateException("권한 변경은 모임장만 가능합니다.");
        }
        if (meeting.getCurrentMemberCount() <= 1) {
            throw new IllegalStateException("모임원이 1명이라 위임할 수 없습니다.");
        }
        // 대상 멤버 조회
        MeetingMember target = meetingMemberRepository.findById(dto.getMeetingMemberId())
                .orElseThrow(() -> new EntityNotFoundException("대상 모임원을 찾을 수 없습니다."));
        // 같은 모임인지 체크
        if (!target.getMeeting().getId().equals(meeting.getId())) {
            throw new IllegalStateException("같은 모임 멤버만 권한 변경 가능합니다.");
        }
        // 승인된 멤버인지 체크
        if (target.getStatus() != MeetingMemberStatus.APPROVED) {
            throw new IllegalStateException("참여 중인 멤버만 권한 변경 가능합니다.");
        }
        // 본인을 모임장으로 다시 지정하는 실수 방지
        if (target.getId().equals(owner.getId())) {
            throw new IllegalArgumentException("본인을 다시 모임장으로 지정할 수 없습니다.");
        }
        // 기존 모임장 내리기
        owner.updateRole(MeetingRole.MEMBER);
        // 대상 모임장으로 승격
        target.updateRole(MeetingRole.OWNER);

    }

    //    모임 탈퇴
    public void leftMeeting(MeetingLeftDto dto) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));


        Meeting meeting = meetingRepository.findByIdAndCrew_IdForUpdate(dto.getMeetingId(), dto.getCrewId())
                .orElseThrow(() -> new EntityNotFoundException("없는 모임 입니다."));
        // 내 모임 멤버 확인
        MeetingMember me = meetingMemberRepository.findByMeeting_IdAndCrewMember_User_IdAndStatus(dto.getMeetingId(), user.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("이 모임에 참여 중인 사람이 아닙니다."));
        // 유료모임인 경우에만 3시간 전 제한
        boolean isPaid = meeting.getFeeType() != MeetingFeeType.FREE && meeting.getFee() > 0;
        if (isPaid) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            LocalDateTime deadline = meeting.getMeetingAt().minusHours(3);
            if (!now.isBefore(deadline)) {
                throw new IllegalStateException("유료모임은 모임 시작 3시간 전까지만 탈퇴할 수 있습니다.");
            }
        }

        // 모임장인 경우
        if (me.getRole() == MeetingRole.OWNER) {

            // 모임장 혼자면 -> 모임 종료 + 본인 LEFT
            if (meeting.getCurrentMemberCount() <= 1) {
                if (meeting.getRecruitStatus() != MeetingRecruitStatus.FINISHED) {
                    meeting.updateStatus(MeetingRecruitStatus.FINISHED);
                } // meeting 상태 FINISHED로 변경
                me.updateStatus(MeetingMemberStatus.LEFT);
                meeting.minusMemberCount(); // 1 -> 0
                return;
            }

            // 다른 모임원이 있으면 -> 차기 모임장 위임 후 본인 LEFT
            if (dto.getNextOwnerMeetingMemberId() == null) {
                throw new IllegalArgumentException("모임장은 탈퇴 전 차기 모임장을 지정해야 합니다.");
            }

            MeetingMember nextOwner = meetingMemberRepository.findById(dto.getNextOwnerMeetingMemberId())
                    .orElseThrow(() -> new IllegalStateException("차기 모임장을 찾을 수 없습니다."));

            // 같은 모임인지
            if (!nextOwner.getMeeting().getId().equals(meeting.getId())) {
                throw new IllegalStateException("차기 모임장은 같은 모임의 멤버여야 합니다.");
            }
            // 참여 중(승인) 멤버인지
            if (nextOwner.getStatus() != MeetingMemberStatus.APPROVED) {
                throw new IllegalStateException("차기 모임장은 참여 중인 멤버여야 합니다.");
            }
            // 본인 지정 방지
            if (nextOwner.getId().equals(me.getId())) {
                throw new IllegalArgumentException("본인을 차기 모임장으로 지정할 수 없습니다.");
            }

            // 위임
            me.updateRole(MeetingRole.MEMBER);
            nextOwner.updateRole(MeetingRole.OWNER);

            // 탈퇴(기록 유지)
            me.updateStatus(MeetingMemberStatus.LEFT);
            meeting.minusMemberCount();
            return;
        }

        // 일반 멤버 탈퇴(기록 유지)
        me.updateStatus(MeetingMemberStatus.LEFT);
        meeting.minusMemberCount();
    }
//    @Transactional(readOnly = true)
//    // 특정 크루원이 가입한 크루 목록 조회
//    public List<CrewMemberJoinedCrewDto> getJoinedMeeting(Long crewId, Long crewMemberId) {
//        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//
//
//        if (!crewMemberRepository.existsByCrewIdAndUserEmailAndStatus(crewId, currentEmail, CrewMemberStatus.APPROVED)) {
//            throw new IllegalStateException("해당 크루의 멤버만 조회할 수 있습니다.");
//        }
//        CrewMember target = crewMemberRepository.findByIdAndCrew_Id(crewMemberId, crewId).orElseThrow(() -> new EntityNotFoundException("해당 크루원을 찾을 수 없습니다."));
//
//        if (target.getStatus() != CrewMemberStatus.APPROVED) {
//            throw new IllegalStateException("승인된 크루원만 조회할 수 있습니다.");
//        }
//
//        // 대상 유저가 APPROVED 상태로 가입한 크루 목록
//        List<CrewMember> joinedCrewMembers = crewMemberRepository.findApprovedCrewsByUserId(target.getUser().getId());
//
//        List<CrewMemberJoinedCrewDto> result = new ArrayList<>();
//        for (CrewMember cm : joinedCrewMembers) {
//            result.add(CrewMemberJoinedCrewDto.fromEntity(cm.getCrew()));
//        }
//        return result;
//    }

}