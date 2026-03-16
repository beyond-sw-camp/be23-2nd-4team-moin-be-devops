package com.team4.moin.crewmember.controller;


import com.team4.moin.crewmember.dtos.*;
import com.team4.moin.crewmember.service.CrewMemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crewMember")
public class CrewMemberController {
    private final CrewMemberService crewMemberService;

    @Autowired
    public CrewMemberController(CrewMemberService crewMemberService) {
        this.crewMemberService = crewMemberService;
    }

    //    가입신청
//{"crewId":1,"joinMessage":"ㅎㅇㅎㅇㅎㅇㅎㅇ"}
    @PostMapping("/request")
    public ResponseEntity<?> crewJoin(@Valid @RequestBody CrewMemberReqDto crewMemberReqDto) {
        Long joinId = crewMemberService.crewJoin(crewMemberReqDto);
        return ResponseEntity.status(HttpStatus.OK).body("크루 가입신청 완료.+ joinId =" + joinId);
    }
    // 가입 신청자 목록 조회 (PENDING)
    @GetMapping("/{crewId}/joinRequestList")
    public ResponseEntity<?> joinRequestList(@PathVariable Long crewId) {
        List<CrewJoinRequestListDto> list = crewMemberService.joinRequestList(crewId);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
    //    가입승인
//    {"crewId":1,"joinId" : 2}
    @PatchMapping("/approved")
    public ResponseEntity<?> approved(@Valid @RequestBody CrewMemberActionDto crewMemberActionDto) {
        crewMemberService.approvedJoin(crewMemberActionDto);
        return ResponseEntity.status(HttpStatus.OK).body("크루 가입신청이 승인되었습니다.");
    }

    //    가입거절
//    {"crewId":1,"joinId" : 2}
    @PatchMapping("/rejected")
    public ResponseEntity<?> rejected(@Valid @RequestBody CrewMemberActionDto crewMemberActionDto) {
        crewMemberService.rejectedJoin(crewMemberActionDto);
        return ResponseEntity.status(HttpStatus.OK).body("크루 가입신청이 거절되었습니다.");
    }

    // 크루원 목록 조회
    @GetMapping("/{crewId}/memberList")
    public ResponseEntity<?> crewMembers(@PathVariable Long crewId) {
        List<CrewMemberListDto> crewMembers = crewMemberService.findCrewMembers(crewId);
        return ResponseEntity.status(HttpStatus.OK).body(crewMembers);
    }
//    크루 운영진 목록 조회
    @GetMapping("/{crewId}/managerList")
    public ResponseEntity<?> crewManagers(@PathVariable Long crewId){
        List<CrewMemberListDto>crewmanagers = crewMemberService.findCrewManagers(crewId);
        return ResponseEntity.status(HttpStatus.OK).body(crewmanagers);
    }

    //    권한변경
//    {"crewId": 1,"crewMemberId": 1,"role": "OWNER"}
    @PatchMapping("/role")
    public ResponseEntity<?> changeRole(@Valid @RequestBody CrewMemberRoleChangeDto dto) {
        crewMemberService.changeRole(dto);
        return ResponseEntity.status(HttpStatus.OK).body("크루 권한이 변경되었습니다.");
    }

    // 크루 나가기
//    {"crewId":1,"leftReason":"개인 사정"} -일반회원,운영진
//    {"crewId":1,"leftReason":"개인 사정","nextOwnerCrewMemberId":2} - 크루장탈퇴시
    @PostMapping("/left")
    public ResponseEntity<String> leftCrew(@RequestBody CrewMemberLeftDto dto) {
        crewMemberService.leftCrew(dto);
        return ResponseEntity.status(HttpStatus.OK).body("크루 탈퇴가 완료되었습니다.");
    }

    @GetMapping("/{crewId}/member/{crewMemberId}")
    public ResponseEntity<?> crewMemberDetail(@PathVariable Long crewId, @PathVariable Long crewMemberId) {
        CrewMemberDetailDto detail = crewMemberService.getCrewMemberDetail(crewId, crewMemberId);
        return ResponseEntity.status(HttpStatus.OK).body(detail);
    }

    // 특정 크루원이 가입한 크루 목록 조회
    @GetMapping("/{crewId}/member/{crewMemberId}/crews")
    public ResponseEntity<?> joinedCrews(@PathVariable Long crewId, @PathVariable Long crewMemberId) {
        List<CrewMemberJoinedCrewDto> result = crewMemberService.getJoinedCrews(crewId, crewMemberId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
