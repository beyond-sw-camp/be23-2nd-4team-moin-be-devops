package com.team4.moin.user.service;

import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crew.repository.CrewFavoriteRepository;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.fee.domain.RefundLog;
import com.team4.moin.fee.domain.RefundType;
import com.team4.moin.fee.domain.SettlementStatus;
import com.team4.moin.fee.repository.RefundLogRepository;
import com.team4.moin.fee.repository.SettlementLogRepository;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingFeeType;
import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import com.team4.moin.meetingmember.repository.MeetingMemberRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.dtos.mypage.MyPageResponseDto;
import com.team4.moin.manner.repository.MannerLogRepository;
import com.team4.moin.user.repository.CategoryRepository;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class MyPageService {

    private final UserRepository userRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CategoryRepository categoryRepository;
    private final CrewFavoriteRepository crewFavoriteRepository;
    private final MeetingRepository meetingRepository;
    private final SettlementLogRepository settlementLogRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final RefundLogRepository refundLogRepository;

    @Autowired
    public MyPageService(UserRepository userRepository,
                         CrewMemberRepository crewMemberRepository,
                         MannerLogRepository mannerLogRepository,
                         CategoryRepository categoryRepository,
                         CrewFavoriteRepository crewFavoriteRepository,
                         MeetingRepository meetingRepository,
                         SettlementLogRepository settlementLogRepository,
                         MeetingMemberRepository meetingMemberRepository,
                         RefundLogRepository refundLogRepository) {
        this.userRepository = userRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.categoryRepository = categoryRepository;
        this.crewFavoriteRepository = crewFavoriteRepository;
        this.meetingRepository = meetingRepository;
        this.settlementLogRepository = settlementLogRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.refundLogRepository = refundLogRepository;
    }

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPageInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // ─── 활동 통계 ────────────────────────────────────────────────
        long totalApprovedCount = crewMemberRepository.countByUser_IdAndStatusAndCrew_DelYn(user.getId(), CrewMemberStatus.APPROVED,"No");
        long pendingCount       = crewMemberRepository.countByUser_IdAndStatusAndCrew_DelYn(user.getId(), CrewMemberStatus.PENDING,"No");
        long leadCount          = crewMemberRepository.countByUser_IdAndRoleAndCrew_DelYn(user.getId(), CrewRole.OWNER,"No");
        long favoriteCount = crewFavoriteRepository.countActiveFavoritesByUserId(user.getId(), "No");
        // 크루장도 APPROVED 상태라 중복 방지
        long memberOnlyCount = Math.max(0, totalApprovedCount - leadCount);

        // ─── 관심 카테고리 ─────────────────────────────────────────────
        List<String> categories = categoryRepository.findByUser(user).stream()
                .map(cat -> cat.getCategoryType().name())
                .toList();

        // ─── 정산 합계 ─────────────────────────────────────────────────
        Long totalFeePoolSum   = meetingRepository.sumTotalFeePoolByOwnerId(user.getId());
        Long alreadySettledSum = settlementLogRepository.sumTotalSettledByOwnerId(user.getId());
        totalFeePoolSum   = (totalFeePoolSum   != null) ? totalFeePoolSum   : 0L;
        alreadySettledSum = (alreadySettledSum != null) ? alreadySettledSum : 0L;


        // ─── 호스트 정산 내역 ──────────────────────────────────────────
        List<MyPageResponseDto.HostSettlementDetail> hostingSettlements =
                meetingRepository.findAllPaidMeetingsByOwnerId(user.getId(), MeetingRole.OWNER, MeetingFeeType.PAID)
                        .stream()
                        .filter(Meeting::isPaid)
                        .map(m -> {
                            Long settled = settlementLogRepository.sumAmountByMeetingIdAndStatus(
                                    m.getId(), SettlementStatus.COMPLETED);

                            Map<Long, RefundType> refundTypeMap = refundLogRepository
                                    .findAllByMeetingId(m.getId())
                                    .stream()
                                    .collect(Collectors.toMap(
                                            rl -> rl.getMeetingMember().getId(),
                                            RefundLog::getRefundType,
                                            (a, b) -> b
                                    ));

                            List<MyPageResponseDto.RefundRequest> refundedMembers =
                                    meetingMemberRepository
                                            .findAllByMeetingIdAndStatus(m.getId(), MeetingMemberStatus.REFUNDED)
                                            .stream()
                                            .map(mm -> MyPageResponseDto.RefundRequest.builder()
                                                    .meetingMemberId(mm.getId())
                                                    .memberName(mm.getCrewMember().getUser().getNickname())
                                                    .refundAmount(m.getFee())
                                                    .requestMessage(mm.getRefundReason())
                                                    .status(mm.getStatus())
                                                    .refundType(refundTypeMap.getOrDefault(mm.getId(), RefundType.USER_REQUEST))
                                                    .requestedAt(mm.getCreatedTime())
                                                    .build())
                                            .toList();

                            return MyPageResponseDto.HostSettlementDetail.fromEntity(
                                    m,
                                    (settled != null) ? settled : 0L,
                                    refundedMembers
                            );
                        })
                        .toList();

        // ─── 내 환불 내역 (참여자 관점) ────────────────────────────────
        List<MyPageResponseDto.UserRefundHistory> refundHistories =
                meetingMemberRepository.findAllByUserIdAndStatusIn(
                                user.getId(),
                                List.of(MeetingMemberStatus.REFUNDED)
                        )
                        .stream()
                        .map(MyPageResponseDto.UserRefundHistory::fromEntity)
                        .toList();

        // ─── 최종 반환 ─────────────────────────────────────────────────
        return MyPageResponseDto.fromEntity(
                user,
                pendingCount,
                memberOnlyCount,
                leadCount,
                categories,
                favoriteCount,
                totalFeePoolSum,
                alreadySettledSum,
                hostingSettlements,
                refundHistories
        );
    }
}