package com.team4.moin.fee.controller;

import com.team4.moin.fee.dtos.RefundApprovalDto;
import com.team4.moin.fee.dtos.RefundRequestDto;
import com.team4.moin.fee.service.RefundFacade;
import com.team4.moin.fee.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fee")
public class FeeController {

    private final SettlementService settlementService;
    private final RefundFacade refundFacade;

    @Autowired
    public FeeController(SettlementService settlementService, RefundFacade refundFacade) {
        this.settlementService = settlementService;
        this.refundFacade = refundFacade;
    }

    // [참여자] 환불 및 참여 취소 요청 (정산 전이면 즉시 환불, 아니면 예외 발생)
    @PostMapping("/refund/{meetingMemberId}")
    public ResponseEntity<String> requestRefund(@PathVariable Long meetingMemberId, @RequestBody RefundRequestDto requestDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Facade로 요청을 넘김 (알아서 처리됨)
        refundFacade.requestRefund(meetingMemberId, requestDto.getReason(), email);

        return ResponseEntity.ok("환불이 정상적으로 완료되었습니다.");
    }

}
