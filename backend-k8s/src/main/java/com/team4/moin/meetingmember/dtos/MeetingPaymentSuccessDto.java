package com.team4.moin.meetingmember.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//회비 결제 성공 dto
public class MeetingPaymentSuccessDto {
    @NotNull
    private Long crewId;
    @NotNull
    private Long meetingId;
    @NotNull
    private String merchantUid; // 우리 서버가 생성했던 주문번호
    @NotNull
    private String impUid;      // 포트원에서 발급한 결제 고유번호 (imp_로 시작하는 것)
}
