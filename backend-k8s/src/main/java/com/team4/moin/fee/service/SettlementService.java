package com.team4.moin.fee.service;

import com.team4.moin.Notification.domain.NotificationType;
import com.team4.moin.Notification.service.NotificationService;
import com.team4.moin.common.service.PortOneService;
import com.team4.moin.fee.domain.*;
import com.team4.moin.fee.repository.FeeLogRepository;
import com.team4.moin.fee.repository.RefundLogRepository;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.fee.repository.SettlementLogRepository;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import com.team4.moin.meetingmember.repository.MeetingMemberRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class SettlementService {

    private final SettlementLogRepository settlementLogRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingRepository meetingRepository;
    private final PortOneService portOneService;
    private final RefundLogRepository refundLogRepository;
    private final FeeLogRepository feeLogRepository;
    private final NotificationService notificationService;

    @Autowired
    public SettlementService(SettlementLogRepository settlementLogRepository, MeetingMemberRepository meetingMemberRepository, MeetingRepository meetingRepository, PortOneService portOneService, RefundLogRepository refundLogRepository, FeeLogRepository feeLogRepository, NotificationService notificationService) {
        this.settlementLogRepository = settlementLogRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.meetingRepository = meetingRepository;
        this.portOneService = portOneService;
        this.refundLogRepository = refundLogRepository;
        this.feeLogRepository = feeLogRepository;
        this.notificationService = notificationService;
    }


    @Transactional(readOnly = true)
    public MeetingMember getRefundTarget(Long meetingMemberId, String email) {
        MeetingMember member = meetingMemberRepository.findByIdWithMeetingAndUser(meetingMemberId)
                .orElseThrow(() -> new EntityNotFoundException("참여 내역이 없습니다."));

        if (!member.getCrewMember().getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("본인의 참여 내역만 환불 요청이 가능합니다.");
        }
        return member;
    }

    @Transactional
    public void applyRefundSuccess(Long meetingMemberId, String reason) {
        MeetingMember member = meetingMemberRepository.findById(meetingMemberId).orElseThrow();
        Meeting meeting = member.getMeeting();

        // 환불 로그
        refundLogRepository.save(RefundLog.builder()
                .meeting(meeting).meetingMember(member).amount(meeting.getFee())
                .impUid(member.getImpUid()).refundReason(reason)
                .refundType(RefundType.USER_REQUEST) // 승인 절차가 사라졌으므로 AUTOMATIC(또는 USER_REQUEST)으로 고정
                .build());

        // Fee 로그
        feeLogRepository.save(FeeLog.builder()
                .meetingId(meeting).email(member.getCrewMember().getUser().getEmail())
                .amount(-meeting.getFee()).status(FeeStatus.REFUND).build());

        meeting.minusFee(meeting.getFee());
        meeting.minusMemberCount();
        member.updateStatus(MeetingMemberStatus.REFUNDED);
        notificationService.send(
                member.getCrewMember().getUser(),
                NotificationType.REFUND,
                "meeting",
                meeting.getId(),
                meeting.getName() + " 모임 환불이 완료되었습니다."
        );
    }

    @Transactional
    public void processFreeMeetingCancel(Long meetingMemberId) {
        MeetingMember member = meetingMemberRepository.findById(meetingMemberId).orElseThrow();
        Meeting meeting = member.getMeeting();
        member.updateStatus(MeetingMemberStatus.REFUNDED);
        meeting.minusMemberCount();
    }

    // --- [정산 관련 로직] ---

    @Transactional
    public void processSettlement(Meeting meeting) {
        Long amountToSettle = meeting.getTotalFeePool();

        if (amountToSettle == null || amountToSettle <= 0) {
            meeting.markAsSettled(); // 금액이 0원이어도 환불을 막기 위해 정산 완료 처리
            return;
        }

        User owner = meetingMemberRepository.findOwnerByMeetingId(meeting.getId())
                .orElseThrow(() -> new EntityNotFoundException("모임장이 없습니다."));

        SettlementLog logRecord = SettlementLog.builder()
                .meeting(meeting).user(owner).amount(amountToSettle)
                .bankName(meeting.getBankName()).accountNumber(meeting.getAccountNumber())
                .build();

        settlementLogRepository.save(logRecord);

        try {
            logRecord.markAsCompleted();
            meeting.markAsSettled(); // [핵심] 모임 정산 완료 처리 (이후 환불 차단)

            notificationService.send(owner, NotificationType.SETTLEMENT_COMPLETE, "meeting", meeting.getId(), "정산이 완료되었습니다.");
        } catch (Exception e) {
            log.error("정산 실패: 모임ID {}", meeting.getId());
            logRecord.markAsFailed();
        }
    }
}

