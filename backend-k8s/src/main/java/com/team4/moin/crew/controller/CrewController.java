package com.team4.moin.crew.controller;

import com.team4.moin.chat.service.ChatService;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.dtos.*;
import com.team4.moin.crew.service.CrewService;
import com.team4.moin.user.domain.entitys.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crew")
public class CrewController {
    private final CrewService crewService;

    @Autowired
    public CrewController(CrewService crewService) {
        this.crewService = crewService;
    }


    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String fileName) {
        String url = crewService.getPresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }
    //크루생성
    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CrewCreateDto crewCreateDto) {
        Long id = crewService.save(crewCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    //크루 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> findCrewList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                                          Pageable pageable, @ModelAttribute CrewSearchDto crewSearchDto) {
        Page<CrewListDto> crewListDto = crewService.findAll(pageable, crewSearchDto);
        return ResponseEntity.status(HttpStatus.OK).body(crewListDto);
    }

    //    크루 인기순 조회(조회 많은 순)
    @GetMapping("/popular")
    public ResponseEntity<List<CrewListDto>> popular(@PageableDefault(size = 5, sort = "viewCount", direction = Sort.Direction.DESC)
                                                     Pageable pageable) {
        List<CrewListDto> popularCrew = crewService.findPopularCrew(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(popularCrew);
    }

//    크루 수정
    @PatchMapping("/update")
    public ResponseEntity<?> updateCrew(@RequestBody CrewUpdateDto dto) {
        crewService.updateCrew(dto);
        return ResponseEntity.status(HttpStatus.OK).body("크루 변경사항 수정 완료");
    }
    //    크루 평점
//    {"crewId": 3, "score": 4.5}
    @PostMapping("/rating")
    public ResponseEntity<?> rateCrew(@Valid @RequestBody CrewRatingDto dto) {
        crewService.rateCrew(dto);
        return ResponseEntity.status(HttpStatus.OK).body("평점추가 완료");
    }
//    추천 크루
    @GetMapping("/recommendations")
    public ResponseEntity<Page<CrewListDto>> getRecommendedCrews(
            @PageableDefault(size = 10) Pageable pageable) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal != null) ? principal.toString() : null;

        Page<CrewListDto> response = crewService.getRecommendedCrewsPage(pageable, email);
        return ResponseEntity.ok(response);
    }
    /**
     * 급상승 루키 크루 목록 조회 (메인 화면 페이징용)
     * GET /crew/rookie?page=0&size=10
     */
    @GetMapping("/rookie")
    public ResponseEntity<Page<CrewListDto>> getRookieCrews(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        Page<CrewListDto> response = crewService.getRookieCrewsPage(pageable, email);
        return ResponseEntity.ok(response);
    }
    // 내가 가입한 크루 목록
    @GetMapping("/myCrew")
    public ResponseEntity<?> findMyCrew() {
        List<MyCrewListDto> result = crewService.findMyCrew();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    @GetMapping("/suggest")
    public ResponseEntity<List<CrewSuggestResponseDto>> suggestCrews(
            @RequestParam(value = "keyword", required = false) String keyword) {

        List<CrewSuggestResponseDto> suggestions = crewService.getCrewSuggestions(keyword);
        return ResponseEntity.ok(suggestions);
    }

    // 1. 참여 대기 중인 크루 목록 (페이징)
    // GET /crew/my-pending?page=0&size=10
    @GetMapping("/my-pending")
    public ResponseEntity<Page<MyCrewListDto>> getPendingCrews(
            @PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MyCrewListDto> response = crewService.getPendingCrews(pageable);
        return ResponseEntity.ok(response);
    }

    // 2. 참여 중인 일반/매니저 크루 목록 (운영자 제외 - 페이징)
    // GET /crew/my-participating?page=0&size=10
    @GetMapping("/my-participating")
    public ResponseEntity<Page<MyCrewListDto>> getParticipatingCrews(
            @PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MyCrewListDto> response = crewService.getParticipatingCrews(pageable);
        return ResponseEntity.ok(response);
    }

    // 3. 운영 중인 크루 목록 (크루장 - 페이징)
    // GET /crew/my-leading?page=0&size=10
    @GetMapping("/my-leading")
    public ResponseEntity<Page<MyCrewListDto>> getLeadingCrews(
            @PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MyCrewListDto> response = crewService.getLeadingCrews(pageable);
        return ResponseEntity.ok(response);
    }

    // 4. 찜한 크루 목록 (페이징)
    // GET /crew/my-favorites?page=0&size=10
    @GetMapping("/my-favorites")
    public ResponseEntity<Page<CrewListDto>> getFavoriteCrews(
            @PageableDefault(size = 10, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        // 찜한 크루는 일반 CrewListDto 반환
        Page<CrewListDto> response = crewService.getFavoriteCrews(pageable);
        return ResponseEntity.ok(response);
    }
    //    크루 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id, HttpServletRequest request) {
        CrewDetailDto dto = crewService.findById(id,request);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
    //    크루삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        crewService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body("크루삭제완료");
    }
}