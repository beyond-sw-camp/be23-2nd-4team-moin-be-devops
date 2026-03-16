package com.team4.moin.report.controller;

import com.team4.moin.report.dtos.ReportCreateDto;
import com.team4.moin.report.dtos.ReportResDto;
import com.team4.moin.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
public class  ReportController {
    private final ReportService reportService;
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 신고 생성
    @PostMapping("/create")
    public ResponseEntity<?> createReport(@RequestBody ReportCreateDto request){

        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createReport(request));
    }

    // 신고 취소
    @PatchMapping("/{reportId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelReport(@PathVariable Long reportId){
        reportService.cancelReport(reportId);
        return ResponseEntity.status(HttpStatus.OK).body("신고 취소가 완료되었습니다.");
    }

}
