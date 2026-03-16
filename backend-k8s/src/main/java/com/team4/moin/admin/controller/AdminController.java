package com.team4.moin.admin.controller;

import com.team4.moin.admin.dtos.*;
import com.team4.moin.admin.service.AdminService;
import com.team4.moin.fee.domain.SettlementLog;
import com.team4.moin.fee.scheduler.SettlementScheduler;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {


    private final AdminService adminService;
    private final SettlementScheduler settlementScheduler;
    @Autowired
    public AdminController(AdminService adminService, SettlementScheduler settlementScheduler) {
        this.adminService = adminService;
        this.settlementScheduler = settlementScheduler;
    }

    @PatchMapping("/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> accept(@RequestBody ReportRequestDto dto) {
        adminService.acceptReport(dto);
        return ResponseEntity.ok("신고 승인 및 처리가 완료되었습니다.");
    }

    @PatchMapping("/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(@RequestBody ReportRequestDto dto) {
        adminService.rejectReport(dto);
        return ResponseEntity.ok("신고가 반려되었습니다.");
    }
    // 1. 승인 대기 중인 유저들 조회 (신고 건수 순)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingReports() {
        List<ReportSummaryResponseDto> responses = adminService.getPendingReportSummary();
        return ResponseEntity.ok(responses);
    }
//      2. 승인 완료된 신고 히스토리 목록
    @GetMapping("/accepted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAcceptedReports() {
        List<ReportHistoryResponseDto> responses = adminService.getAcceptedReportHistory();
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/user/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> userList(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<UserListDto> response = adminService.findByAll(pageable);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> userDetail(@PathVariable Long id){

        UserDetailDto dto = adminService.findById(id);
        return ResponseEntity.ok(dto);
    }
    @PatchMapping("/manner")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMannerScore(@RequestBody MannerUpdateDto dto) {
        adminService.updateUserMannerScore(dto);
        return ResponseEntity.ok("매너 점수가 성공적으로 조정되었습니다.");
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLogs(@RequestParam(value = "search", required = false) String search, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(adminService.getSettlementLogs(search, pageable));
    }

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceRunSettlement() {
        settlementScheduler.hourlySettlementJob();
        return ResponseEntity.ok("정산 스케줄러가 수동으로 실행되었습니다.");
    }
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}