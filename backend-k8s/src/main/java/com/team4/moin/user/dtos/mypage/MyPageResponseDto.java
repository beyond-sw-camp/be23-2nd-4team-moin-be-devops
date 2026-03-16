package com.team4.moin.user.dtos.mypage;

import com.team4.moin.fee.domain.RefundType;
import com.team4.moin.fee.domain.SettlementStatus;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import com.team4.moin.user.domain.entitys.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyPageResponseDto {
    private ProfileInfo profile;
    private ActivityStats activity;
    private SettlementStats settlement;

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ProfileInfo {
        private String nickname;
        private String profileImage;
        private BigDecimal mannerScore;
        private List<String> categories;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ActivityStats {
        private long pendingCount;
        private long approvedCount;
        private long leadingCount;
        private long favoriteCount;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class FavoriteInfo {
        private Long id;
        private String name;
        private String imageUrl;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class SettlementStats {
        private List<HostSettlementDetail> hostingSettlements;
        private List<UserRefundHistory> refundHistories;
    }

    @Data @Builder
    public static class HostSettlementDetail {
        private Long meetingId;
        private String meetingName;
        private Long totalFeePool;           // 현재 모인 회비 (환불 시 깎임)
        private Long expectedAmount;         // 정산 예정 금액
        private Long alreadySettled;         // 정산 완료 금액
        private SettlementStatus status;
        private List<RefundRequest> refundedMembers; // 환불 완료된 멤버 목록
        private Long totalRefundedAmount;            // 환불 완료 총액

        // [빨간불 해결 2] 파라미터를 3개만 받도록 수정!
        public static HostSettlementDetail fromEntity(
                Meeting meeting,
                Long settledAmount,
                List<RefundRequest> refundedMembers
        ) {
            // 환불 완료 총액 계산
            Long totalRefunded = refundedMembers == null ? 0L :
                    refundedMembers.stream()
                            .mapToLong(r -> r.getRefundAmount() != null ? r.getRefundAmount() : 0L)
                            .sum();

            // 정산 예정액: 현재 모인 회비(totalFeePool) - 이미 정산된 금액
            Long expectedAmount = Math.max(0, meeting.getTotalFeePool() - settledAmount);

            // 상태 결정: 예정액이 0이고 정산된 금액이 있으면 완료
            SettlementStatus currentStatus = (expectedAmount == 0 && settledAmount > 0)
                    ? SettlementStatus.COMPLETED
                    : SettlementStatus.PENDING;

            return HostSettlementDetail.builder()
                    .meetingId(meeting.getId())
                    .meetingName(meeting.getName())
                    .totalFeePool(meeting.getTotalFeePool())
                    .alreadySettled(settledAmount)
                    .expectedAmount(expectedAmount)
                    .totalRefundedAmount(totalRefunded)
                    .status(currentStatus)
                    .refundedMembers(refundedMembers) // 대기자 목록 삭제됨
                    .build();
        }
    }

    @Data @Builder
    public static class RefundRequest {
        private Long meetingMemberId;
        private String memberName;
        private Long refundAmount;
        private String requestMessage;
        private MeetingMemberStatus status;  // 무조건 REFUNDED 만 들어옴
        private RefundType refundType;       // 무조건 USER_REQUEST 만 들어옴
        private LocalDateTime requestedAt;
    }

    @Data @Builder
    public static class UserRefundHistory {
        private String meetingName;
        private Long amount;
        private MeetingMemberStatus status;
        private LocalDateTime date;

        public static UserRefundHistory fromEntity(MeetingMember mm) {
            return UserRefundHistory.builder()
                    .meetingName(mm.getMeeting().getName())
                    .amount(mm.getMeeting().getFee())
                    .status(mm.getStatus())
                    .date(mm.getCreatedTime())
                    .build();
        }
    }

    public static MyPageResponseDto fromEntity(
            User user, long pendingCount, long approvedCount, long leadCount,
            List<String> categories, long favoriteCount,
            Long totalFeePool, Long alreadySettled,
            List<HostSettlementDetail> hostingSettlements, List<UserRefundHistory> refundHistories
    ) {
        return MyPageResponseDto.builder()
                .profile(ProfileInfo.builder().nickname(user.getNickname()).profileImage(user.getProfileImageUrl()).mannerScore(user.getMannerScore()).categories(categories).build())
                .activity(ActivityStats.builder().pendingCount(pendingCount).approvedCount(approvedCount).leadingCount(leadCount).favoriteCount(favoriteCount).build())
                .settlement(SettlementStats.builder().hostingSettlements(hostingSettlements).refundHistories(refundHistories).build())
                .build();
    }
}