package com.team4.moin.manner.controller;

import com.team4.moin.manner.dtos.manner.AttendanceRequestDto;
import com.team4.moin.manner.dtos.manner.EvaluationRequestDto;
import com.team4.moin.manner.service.MannerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
@Slf4j
@RestController
@RequestMapping("/manner")
public class MannerController {

    private final MannerService mannerService;

    @Autowired
    public MannerController(MannerService mannerService) {
        this.mannerService = mannerService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateMember(@Valid @RequestBody EvaluationRequestDto dto) {
        log.info("EVALUATE dto crewId={}, meetingId={}, targetUserId={}, type={}",
                dto.getCrewId(), dto.getMeetingId(), dto.getTargetUserId(), dto.getEvaluation());
        BigDecimal updatedScore = mannerService.evaluateMember(dto);
        return ResponseEntity.ok(updatedScore);
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> processAttendance(@RequestBody AttendanceRequestDto dto) {
        BigDecimal updatedScore = mannerService.processAttendanceScore(dto);
        return ResponseEntity.ok(updatedScore);
    }
}