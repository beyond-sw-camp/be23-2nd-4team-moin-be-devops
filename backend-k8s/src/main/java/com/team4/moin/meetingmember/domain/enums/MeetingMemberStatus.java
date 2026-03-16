package com.team4.moin.meetingmember.domain.enums;

public enum MeetingMemberStatus {
    PENDING,    // 결제 및 가입 대기
    APPROVED,   // 결제 및 완료/참여 중
    REFUND_REQUESTED, //  정산 후 환불 요청 중 (모임장 승인 대기)
    REFUNDED,   //  환불 완료
    REJECTED,    //  환불 거절
    LEFT //모임 탈퇴
}
