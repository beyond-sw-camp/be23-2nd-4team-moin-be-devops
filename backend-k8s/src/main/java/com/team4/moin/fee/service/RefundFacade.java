package com.team4.moin.fee.service;

import com.team4.moin.common.service.PortOneService;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefundFacade {
    private final SettlementService settlementService;
    private final PortOneService portOneService;

    @Autowired
    public RefundFacade(SettlementService settlementService, PortOneService portOneService) {
        this.settlementService = settlementService;
        this.portOneService = portOneService;
    }
    public void requestRefund(Long meetingMemberId, String reason, String email) {
        MeetingMember member = settlementService.getRefundTarget(meetingMemberId, email);
        Meeting meeting = member.getMeeting();

        if (meeting.isSettled()) {
            throw new IllegalStateException("모임 시작 3시간 전이 지나 정산이 완료되었으므로 환불이 불가능합니다.");
        }

        if (!meeting.isPaid()) {
            settlementService.processFreeMeetingCancel(meetingMemberId);
            return;
        }

        portOneService.cancelPayment(member.getImpUid(), meeting.getFee(), reason);

        settlementService.applyRefundSuccess(meetingMemberId, reason);
    }
}
