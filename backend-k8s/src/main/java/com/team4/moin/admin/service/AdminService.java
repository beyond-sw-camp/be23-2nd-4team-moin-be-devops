package com.team4.moin.admin.service;

import com.team4.moin.admin.dtos.*;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.fee.domain.SettlementLog;
import com.team4.moin.fee.repository.SettlementLogRepository;
import com.team4.moin.feed.domain.Comment;
import com.team4.moin.feed.repository.CommentRepository;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.report.domain.Report;
import com.team4.moin.report.domain.ReportStatus;
import com.team4.moin.report.domain.ReportTargetType;
import com.team4.moin.report.dtos.ReportCountDto;
import com.team4.moin.report.repository.ReportRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SettlementLogRepository settlementLogRepository;
    private final CommentRepository commentRepository;
    @Autowired
    public AdminService(ReportRepository reportRepository, UserRepository userRepository, MeetingRepository meetingRepository, @Qualifier("rtInventory")RedisTemplate<String, String> redisTemplate, SettlementLogRepository settlementLogRepository, CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
        this.redisTemplate = redisTemplate;
        this.settlementLogRepository = settlementLogRepository;
        this.commentRepository = commentRepository;
    }

    public void acceptReport(ReportRequestDto dto) {
        Report report = reportRepository.findById(dto.getReportId())
                .orElseThrow(() -> new EntityNotFoundException("신고 내역을 찾을 수 없습니다."));

        // 이미 거절(REJECTED)된 신고는 다시 승인할 수 없게 막음
        if (report.getStatus() == ReportStatus.REJECTED) {
            throw new IllegalStateException("이미 거절 처리된 신고는 승인할 수 없습니다.");
        }


        // 1. 상태 변경
        report.changeStatus(ReportStatus.ACCEPTED);

        // 2. 신고 횟수 조회 (현재 승인 건을 포함한 최신 카운트)
        long totalReportedCount = reportRepository.countByTargetId(report.getTargetId());

        // 3. 누적 신고 10회 이상 시 제재 실행
        if (totalReportedCount >= 10) {
            ReportTargetType type = report.getTargetType();

            // 유저, 노쇼, 댓글 관련인 경우
            if (type == ReportTargetType.USER ||
                    type == ReportTargetType.NOSHOW ||
                    type == ReportTargetType.COMMENT) {

                User user = userRepository.findById(report.getTargetId()).orElseThrow(()->new EntityNotFoundException("없는 회원입입니다."));
                            // 이미 삭제/정지된 유저가 아닐 때만 실행
                            if (user != null && user.getDelYn().equals("No")) {
                                redisTemplate.delete(user.getEmail());
                                user.updateDelete();
                            }

                            }
                            else if (type == ReportTargetType.MEETING) {
                            meetingRepository.deleteById(report.getTargetId());
        }

        }
    }


//      신고 거절

    public void rejectReport(ReportRequestDto dto) {
        Report report = reportRepository.findById(dto.getReportId())
                .orElseThrow(() -> new EntityNotFoundException("신고 내역을 찾을 수 없습니다."));

        // 이미 승인(ACCEPTED)된 신고는 거절 처리 불가
        if (report.getStatus() == ReportStatus.ACCEPTED) {
            throw new IllegalStateException("이미 승인 처리된 신고는 거절할 수 없습니다.");
        }


        // 상태 변경
        report.changeStatus(ReportStatus.REJECTED);
    }

    public List<ReportSummaryResponseDto> getPendingReportSummary() {
        List<Report> allPending = reportRepository.findAllByStatus(ReportStatus.PENDING);

        if (allPending.isEmpty()) return new ArrayList<>();

        Map<ReportTargetType, Set<Long>> idsByType = allPending.stream()
                .collect(Collectors.groupingBy(
                        Report::getTargetType,
                        Collectors.mapping(Report::getTargetId, Collectors.toSet())
                ));

        // 신고 대상 User 맵
        Set<Long> targetUserIds = new HashSet<>();
        if (idsByType.containsKey(ReportTargetType.USER))
            targetUserIds.addAll(idsByType.get(ReportTargetType.USER));
        if (idsByType.containsKey(ReportTargetType.NOSHOW))
            targetUserIds.addAll(idsByType.get(ReportTargetType.NOSHOW));

        Map<Long, User> userMap = targetUserIds.isEmpty() ? new HashMap<>() :
                userRepository.findAllById(targetUserIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, Meeting> meetingMap = new HashMap<>();
        if (idsByType.containsKey(ReportTargetType.MEETING) && !idsByType.get(ReportTargetType.MEETING).isEmpty()) {
            meetingMap = meetingRepository.findAllById(idsByType.get(ReportTargetType.MEETING)).stream()
                    .collect(Collectors.toMap(Meeting::getId, m -> m));
        }

        Map<Long, Comment> commentMap = new HashMap<>();
        if (idsByType.containsKey(ReportTargetType.COMMENT) && !idsByType.get(ReportTargetType.COMMENT).isEmpty()) {
            commentMap = commentRepository.findAllByIdWithUser(idsByType.get(ReportTargetType.COMMENT)).stream()
                    .collect(Collectors.toMap(Comment::getId, c -> c));
        }

        List<ReportSummaryResponseDto> result = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();

        for (Report report : allPending) {
            Long reportId = report.getId();
            Long targetId = report.getTargetId();
            ReportTargetType type = report.getTargetType();
            String uniqueKey = type.name() + "_" + targetId;

            if (processedKeys.contains(uniqueKey)) continue;

            User reporter = report.getUser();        // 신고자
            String content = report.getReasonText(); // 신고 내용

            switch (type) {
                case USER:
                    if (userMap.containsKey(targetId)) {
                        result.add(ReportSummaryResponseDto.fromUser(reportId, reporter, userMap.get(targetId), content));
                        processedKeys.add(uniqueKey);
                    }
                    break;
                case MEETING:
                    if (meetingMap.containsKey(targetId)) {
                        result.add(ReportSummaryResponseDto.fromMeeting(reportId, reporter, meetingMap.get(targetId), content));
                        processedKeys.add(uniqueKey);
                    }
                    break;
                case NOSHOW:
                    if (userMap.containsKey(targetId)) {
                        result.add(ReportSummaryResponseDto.fromNoShow(reportId, reporter, userMap.get(targetId), content));
                        processedKeys.add(uniqueKey);
                    }
                    break;
                case COMMENT:
                    if (commentMap.containsKey(targetId)) {
                        result.add(ReportSummaryResponseDto.fromComment(reportId, reporter, commentMap.get(targetId), content));
                        processedKeys.add(uniqueKey);
                    }
                    break;
            }
        }

        return result;
    }


    // 2. 히스토리 조회 수정
    public List<ReportHistoryResponseDto> getAcceptedReportHistory() {
        List<Report> acceptedReports = reportRepository.findAllByStatus(ReportStatus.ACCEPTED);
        if (acceptedReports.isEmpty()) return new ArrayList<>();

        // 타입별 targetId 그룹화
        Map<ReportTargetType, Set<Long>> idsByType = acceptedReports.stream()
                .collect(Collectors.groupingBy(
                        Report::getTargetType,
                        Collectors.mapping(Report::getTargetId, Collectors.toSet())
                ));

        // ── 이메일 맵 ──────────────────────────────────────────────

        // USER, NOSHOW → 유저 이메일
        Set<Long> userTargetIds = new HashSet<>();
        if (idsByType.containsKey(ReportTargetType.USER))
            userTargetIds.addAll(idsByType.get(ReportTargetType.USER));
        if (idsByType.containsKey(ReportTargetType.NOSHOW))
            userTargetIds.addAll(idsByType.get(ReportTargetType.NOSHOW));

        Map<Long, String> userEmailMap = userTargetIds.isEmpty() ? new HashMap<>() :
                userRepository.findAllById(userTargetIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getEmail));

        // MEETING → 모임 이름
        Map<Long, String> meetingEmailMap = new HashMap<>();
        Set<Long> meetingIds = idsByType.getOrDefault(ReportTargetType.MEETING, new HashSet<>());
        if (!meetingIds.isEmpty()) {
            meetingEmailMap = meetingRepository.findAllById(meetingIds).stream()
                    .collect(Collectors.toMap(Meeting::getId, m -> "[모임] " + m.getName()));
        }

        // COMMENT → 댓글 작성자 이메일 + commentId → userId 맵
        Map<Long, String> commentEmailMap = new HashMap<>();
        Map<Long, Long> commentToUserIdMap = new HashMap<>();
        Set<Long> commentIds = idsByType.getOrDefault(ReportTargetType.COMMENT, new HashSet<>());
        if (!commentIds.isEmpty()) {
            List<Comment> comments = commentRepository.findAllByIdWithUser(commentIds);
            commentEmailMap = comments.stream()
                    .collect(Collectors.toMap(Comment::getId, c -> c.getUser().getEmail()));
            commentToUserIdMap = comments.stream()
                    .collect(Collectors.toMap(Comment::getId, c -> c.getUser().getId()));
        }

        // ── 신고 수 맵 (N+1 방지 - 한 번에 조회) ───────────────────

        // USER, NOSHOW → 유저 ID 기준 ACCEPTED 신고 수
        Map<Long, Long> userCountMap = userTargetIds.isEmpty() ? new HashMap<>() :
                reportRepository.countByTargetIdsAndStatus(userTargetIds, ReportStatus.ACCEPTED).stream()
                        .collect(Collectors.toMap(ReportCountDto::getTargetId, ReportCountDto::getCount));

        // MEETING → 모임 ID 기준 ACCEPTED 신고 수
        Map<Long, Long> meetingCountMap = meetingIds.isEmpty() ? new HashMap<>() :
                reportRepository.countByTargetIdsAndStatus(meetingIds, ReportStatus.ACCEPTED).stream()
                        .collect(Collectors.toMap(ReportCountDto::getTargetId, ReportCountDto::getCount));

        // COMMENT → 댓글 작성자 userId 기준 ACCEPTED 신고 수
        Map<Long, Long> commentCountMap = commentIds.isEmpty() ? new HashMap<>() :
                reportRepository.countByTargetIdsAndStatus(commentIds, ReportStatus.ACCEPTED).stream()
                        .collect(Collectors.toMap(ReportCountDto::getTargetId, ReportCountDto::getCount));

        // ── final 변수 (람다 사용을 위해) ──────────────────────────
        final Map<Long, String> finalMeetingEmailMap = meetingEmailMap;
        final Map<Long, String> finalCommentEmailMap = commentEmailMap;
        final Map<Long, Long> finalCommentToUserIdMap = commentToUserIdMap;

        // ── DTO 변환 ───────────────────────────────────────────────
        return acceptedReports.stream()
                .map(report -> {
                    Long targetId = report.getTargetId();
                    ReportTargetType type = report.getTargetType();

                    String email = switch (type) {
                        case USER, NOSHOW -> userEmailMap.getOrDefault(targetId, "알 수 없음");
                        case MEETING      -> finalMeetingEmailMap.getOrDefault(targetId, "알 수 없음");
                        case COMMENT      -> finalCommentEmailMap.getOrDefault(targetId, "알 수 없음");
                        default           -> "알 수 없음";
                    };

                    long reportedCount = switch (type) {
                        case USER, NOSHOW -> userCountMap.getOrDefault(targetId, 0L);
                        case MEETING      -> meetingCountMap.getOrDefault(targetId, 0L);
                        case COMMENT      -> commentCountMap.getOrDefault(targetId, 0L);
                        default -> 0L;
                    };

                    return ReportHistoryResponseDto.fromEntity(report, email, reportedCount);
                })
                .sorted((a, b) -> b.getReportId().compareTo(a.getReportId()))
                .toList();
    }
    @Transactional(readOnly = true)
    public Page<UserListDto> findByAll(Pageable pageable) {

        Page<User> users = userRepository.findAllWithAddress(pageable);
        Page<UserListDto> dto = users.map(UserListDto::fromEntity);
        return dto;
    }
    @Transactional(readOnly = true)
    public UserDetailDto findById(Long id){
        Optional<User> optuser = userRepository.findById(id);
        User user = optuser.orElseThrow(()-> new EntityNotFoundException("아이디가 없습니다"));
        UserDetailDto dto = UserDetailDto.fromEntity(user);
        return dto;
    }
    public void updateUserMannerScore(MannerUpdateDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        user.updateMannerScore(dto.getDelta());

        // 영속성 save 안해도됨
    }

    @Transactional(readOnly = true)
    public Page<SettlementLogResDto> getSettlementLogs(String search, Pageable pageable) {
        Page<SettlementLog> logs = settlementLogRepository.findAllWithSearch(search, pageable);

        return logs.map(SettlementLogResDto::fromEntity);
    }
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        return DashboardStatsDto.builder()
                .totalUsers(userRepository.count())
                .pendingReportCount(reportRepository.countByStatus(ReportStatus.PENDING))
                .acceptedReportCount(reportRepository.countByStatus(ReportStatus.ACCEPTED))
                .totalSettlementCount(settlementLogRepository.count())
                .build();
    }
}