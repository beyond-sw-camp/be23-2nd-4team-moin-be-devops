package com.team4.moin.report.service;


import com.team4.moin.report.domain.Report;
import com.team4.moin.report.domain.ReportStatus;
import com.team4.moin.report.dtos.ReportCreateDto;
import com.team4.moin.report.dtos.ReportResDto;
import com.team4.moin.report.repository.ReportRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    @Autowired
    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    // 신고 생성
    public ReportResDto createReport(ReportCreateDto dto){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByEmailAndDelYn(email, "No")
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        // 중복 신고 체크
        reportRepository.findByUserIdAndTargetTypeAndTargetId(
                user.getId(),
                dto.getTargetType(),
                dto.getTargetId()
        ).ifPresent(r -> {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        });

        Report report = dto.toEntity(user);
        reportRepository.save(report);

        return ReportResDto.fromEntity(report);
    }

    // 신고 취소
    public void cancelReport(Long reportId){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByEmailAndDelYn(email, "No")
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("신고 내역을 찾을 수 없습니다."));

        if(!report.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("본인 신고만 취소 가능 합니다.");
        }

        report.changeStatus(ReportStatus.CANCELED);
    }



}
