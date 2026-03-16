package com.team4.moin.meeting.controller;

import com.team4.moin.crew.dtos.CrewCreateDto;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.dtos.*;
import com.team4.moin.meeting.dtos.route.MeetingRouteReqDto;
import com.team4.moin.meeting.service.MeetingRouteService;
import com.team4.moin.meeting.service.MeetingService;
import com.team4.moin.meeting.service.TmapService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;

@RestController
@RequestMapping("/meeting")
public class MeetingController {
    public final MeetingService meetingService;
    public final MeetingRouteService meetingRouteService;
    private final TmapService tmapService;
    @Autowired
    public MeetingController(MeetingService meetingService, MeetingRouteService meetingRouteService, TmapService tmapService) {
        this.meetingService = meetingService;
        this.meetingRouteService = meetingRouteService;
        this.tmapService = tmapService;
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String fileName) {
        String url = meetingService.getPresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }
//    모임 생성
//{"meetingName": "스트레스 많이 받을거야", "place": "서울 신대방","maxMembers": 10, "description":"자기전에 생각날거야","meetingAt": "2026-02-15T14:00:00"}

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody MeetingCreateDto meetingCreateDto) {
        Long id = meetingService.save(meetingCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);

    }
//    모임 목록 조회
    @GetMapping("/crew/{crewId}/list")
    public ResponseEntity<?>findAll(@PathVariable Long crewId,
                                    @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable ){
        Page<MeetingListDto>meetingListDto = meetingService.findAll(crewId,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(meetingListDto);

    }

    // 모임 수정
    @PatchMapping("/update")
    public ResponseEntity<?> update(@RequestBody MeetingUpdateDto meetingUpdateDto) {
        meetingService.update( meetingUpdateDto);
        return ResponseEntity.status(HttpStatus.OK).body("모임 수정 완료");
    }

//    모임 상태 변경
    @PatchMapping("/status")
    public ResponseEntity<?> changeStatus(@RequestBody MeetingStatusDto meetingStatusDto) {
    meetingService.changeStatus(meetingStatusDto);
    return ResponseEntity.status(HttpStatus.OK).body("모임 상태 변경 완료");
}
    //    모임 종료후(참여/노쇼)모임원 매너점수 증감
    @PostMapping("/finish")
    public ResponseEntity<String> finishMeeting(@Valid @RequestBody MeetingFinishedAttendanceDto dto) {
        meetingService.finishMeetingAndApplyAttendance(dto);
        return ResponseEntity.status(HttpStatus.OK).body("모임 종료 처리 완료(참석/노쇼 매너점수 반영 완료)");
    }

//    길찾기
    @PostMapping("/route/raw")
    public ResponseEntity<String> routeRaw(@Valid @RequestBody MeetingRouteReqDto dto) {
        String raw = meetingRouteService.routeRaw(dto);
        return ResponseEntity.status(HttpStatus.OK).body(raw);
    }
    @PostMapping("/route/summary")
    public ResponseEntity<?> routeSummary(@Valid @RequestBody MeetingRouteReqDto dto) {
        System.out.println(">>> routeSummary HIT: " + dto);
        return ResponseEntity.ok(meetingRouteService.routeSummary(dto));
    }
    /**
     * 프론트엔드 호출 경로: GET /meeting/imminent?crewId=1
     */
    @GetMapping("/imminent")
    public ResponseEntity<List<MeetingImminentDto>> getImminentMeetings(
            @RequestParam("crewId") Long crewId) {

        List<MeetingImminentDto> response = meetingService.getImminentMeetings(crewId);
        return ResponseEntity.ok(response);
    }

    // 내 모임 일정 조회
    @GetMapping("/my-meeting")
    public ResponseEntity<?> findMyMeetings() {
        List<MyMeetingListDto> result = meetingService.findMyMeetings();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
//    //크루 내 모든 모임 일정 조회
//    @GetMapping("/crew-meetings")
//    public ResponseEntity<Page<MeetingListDto>> getCrewMeetings(
//            @PageableDefault(size = 10, sort = "meetingAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//        Page<MeetingListDto> response = meetingService.getCrewMeetings(pageable);
//        return ResponseEntity.ok(response);
//    }
    // 내 모임 일정 조회
    @GetMapping("/my-my-past-meeting") // 경로 충돌을 피하기 위한 설정
    public ResponseEntity<Page<MyMeetingListDto>> findFinishedMyMeetings(Pageable pageable) {
        Page<MyMeetingListDto> result = meetingService.findFinishedMyMeetings(pageable);
        return ResponseEntity.ok(result);
    }
    //    모임 상세 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<?>findById(@PathVariable Long meetingId ){
        MeetingDetailDto dto = meetingService.findById(meetingId);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
    // 모임 삭제
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<?> delete(@PathVariable Long meetingId) {
        meetingService.delete(meetingId);
        return ResponseEntity.status(HttpStatus.OK).body("모임 삭제 완료");
    }
    /**
     *  장소 후보 리스트 API
     * 프론트: GET /meeting/place/candidates?query=사용자가 입력한 주소
     * 응답: [{name, address, latitude, longitude}, ...]
     */
    @GetMapping("/place/candidates")
    public ResponseEntity<List<MeetingMapDto>> searchPlaceCandidates(@RequestParam String query) {
        List<MeetingMapDto> result = tmapService.searchCandidates(query);
        return ResponseEntity.ok(result);
    }
}
