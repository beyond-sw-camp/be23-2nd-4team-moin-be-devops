package com.team4.moin.fee.scheduler;

import com.team4.moin.fee.service.SettlementService;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingFeeType;
import com.team4.moin.meeting.repository.MeetingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class SettlementScheduler {

    private final MeetingRepository meetingRepository;
    private final SettlementService settlementService;

    @Autowired
    public SettlementScheduler(MeetingRepository meetingRepository, SettlementService settlementService) {
        this.meetingRepository = meetingRepository;
        this.settlementService = settlementService;
    }
    // 매시간 0분에 실행 (예: 13:00, 14:00 ...)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void hourlySettlementJob() {
        log.info("=== 매시간 모임 자동 정산 스케줄러 시작 ===");

        // 모임 시작 시간이 현재로부터 3시간 이내인 모임 타겟팅
        LocalDateTime targetTime = LocalDateTime.now().plusHours(3);
        log.info("조회 기준 시간 (현재+3시간): {}", targetTime);
        List<Meeting> meetingsToSettle = meetingRepository.findMeetingsToSettle(targetTime);
        log.info("조회된 정산 대상 모임 수: {}", meetingsToSettle.size());
        if (meetingsToSettle.isEmpty()) {
            log.info("현재 정산 대상인 모임이 없습니다.");
            return;
        }

        for (Meeting meeting : meetingsToSettle) {
            try {
                settlementService.processSettlement(meeting);
                log.info("모임 ID {} 정산 완료", meeting.getId());
            } catch (Exception e) {
                log.error("모임 ID {} 정산 중 예외 발생: {}", meeting.getId(), e.getMessage());
            }
        }

        log.info("=== 자동 정산 스케줄러 종료 ===");
    }
}
