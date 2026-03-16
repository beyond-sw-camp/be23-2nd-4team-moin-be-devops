package com.team4.moin.meetingmember.dtos;

import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingJoinResDto {

    private Long joinId;          // 신청 기록 PK
    private boolean approved;     // 즉시 승인 여부 (무료 모임인 경우 true)

    // --- 포트원 결제창(SDK) 호출에 필수적인 필드들 ---
    private String merchantUid;   // 서버에서 생성한 주문번호 (MEET_1_user1_12345...)
    private Long amount;          // 결제 예정 금액 (회비)
    private String meetingName;   // 결제창에 표시될 상품명 (모임 이름)
    private String buyerEmail;    // 결제자 연락처 (포트원 기본 정보)


    public static MeetingJoinResDto fromEntity(MeetingMember meetingMember, boolean approved) {
        return MeetingJoinResDto.builder()
                .joinId(meetingMember.getId())
                .approved(approved)
                // 엔티티에 추가한 merchantUid 필드를 호출합니다.
                .merchantUid(meetingMember.getMerchantUid())
                // 연관관계(Meeting)를 통해 금액과 이름을 가져옵니다.
                .amount(meetingMember.getMeeting().getFee())
                .meetingName(meetingMember.getMeeting().getName())
                // 연관관계(User)를 통해 이메일을 가져옵니다.
                .buyerEmail(meetingMember.getCrewMember().getUser().getEmail())
                .build();
    }
}
