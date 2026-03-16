package com.team4.moin.meetingmember.controller;


import com.team4.moin.crewmember.dtos.CrewMemberJoinedCrewDto;
import com.team4.moin.meetingmember.dtos.*;
import com.team4.moin.meetingmember.service.MeetingMemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meetingMember")
public class MeetingMemberController {
    private final MeetingMemberService meetingMemberService;

    @Autowired
    public MeetingMemberController(MeetingMemberService meetingMemberService) {
        this.meetingMemberService = meetingMemberService;
    }

    // 가입 신청
    // {"crewId":1,"meetingId":1,"joinMessage":"ㅎㅇㅎㅇㅎㅇ"}
    @PostMapping("/request")
    public ResponseEntity<MeetingJoinResDto> meetingJoin(@Valid @RequestBody MeetingMemberReqDto dto) {
        return ResponseEntity.ok(meetingMemberService.meetingJoin(dto));
    }

    @PostMapping("/payment-success")
    public ResponseEntity<String> paymentSuccess(@RequestBody MeetingPaymentSuccessDto dto) {
        meetingMemberService.paymentSuccess(dto);
        return ResponseEntity.ok("결제 승인 완료");
    }


//       결제 실패/중단 시 호출

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody Map<String, String> request) {
        meetingMemberService.cancelPayment(request.get("merchantUid"));
        return ResponseEntity.ok("신청 취소 완료");
    }

    //    모임원 매너점수 개인평가
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@Valid @RequestBody MeetingEvaluationRequestDto dto) {

        BigDecimal resultScore = meetingMemberService.evaluateMeetingMember(dto);

        return ResponseEntity.status(HttpStatus.OK).body("평가가 완료되었습니다. 현재 매너 점수: " + resultScore);
    }
//    모임원 목록조회

    @GetMapping("/{crewId}/{meetingId}/memberList")
    public ResponseEntity<?> meetingMembers( @PathVariable Long crewId,@PathVariable Long meetingId) {
        List<MeetingMemberListDto> meetingMembers = meetingMemberService.findMeetingMembers(crewId,meetingId);
        return ResponseEntity.status(HttpStatus.OK).body(meetingMembers);
    }

    //    모임 권한 변경
//{"crewId": 1, "meetingId": 1,"meetingMemberId":1 }
    @PostMapping("/owner-change")
    public ResponseEntity<?> changeMeetingOwner(@RequestBody MeetingRoleChangeDto dto) {
        meetingMemberService.changeMeetingRole(dto);
        return ResponseEntity.status(HttpStatus.OK).body("모임장 위임 완료");
    }
    // 모임 나가기
//    {"crewId": 1,"meetingId": 1,"leftReason": "개인 사정"}
//    {"crewId": 1,"meetingId": 1,"leftReason": "개인 사정","nextOwnerMeetingMemberId": 2} -모임장 모임 탈퇴시
    @PostMapping("/left")
    public ResponseEntity<?> leftMeeting(@RequestBody MeetingLeftDto dto) {
        meetingMemberService.leftMeeting(dto);
        return ResponseEntity.status(HttpStatus.OK).body("모임 나가기 완료");
    }

//    @GetMapping("/{meetingId}/member/{meetingMemberId}/meeting")
//    public ResponseEntity<?> joinedCrews(@PathVariable Long meetingId, @PathVariable Long meetingMemberId) {
//        List<CrewMemberJoinedCrewDto> result = meetingMemberService.getJoinedMeeting(meetingId, meetingMemberId);
//        return ResponseEntity.status(HttpStatus.OK).body(result);
//    }
}


