package com.team4.moin.meeting.service;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.manner.dtos.manner.AttendanceRequestDto;
import com.team4.moin.manner.service.MannerService;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingFeeType;
import com.team4.moin.meeting.domain.enums.MeetingRecruitStatus;
import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meeting.dtos.*;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.Notification.domain.NotificationType;
import com.team4.moin.Notification.service.NotificationService;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import com.team4.moin.meetingmember.repository.MeetingMemberRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.EvaluationType;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Transactional
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MannerService mannerService;
    private final TmapService tmapService;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${aws.s3.bucket5}")
    private String bucket;
    @Autowired
    public MeetingService(MeetingRepository meetingRepository, MeetingMemberRepository meetingMemberRepository, CrewRepository crewRepository, CrewMemberRepository crewMemberRepository, UserRepository userRepository, NotificationService notificationService, MannerService mannerService, TmapService tmapService, S3Client s3Client, S3Presigner s3Presigner) {
        this.meetingRepository = meetingRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.mannerService = mannerService;

        this.tmapService = tmapService;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    //    모임생성
    public Long save( MeetingCreateDto meetingCreateDto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = meetingCreateDto.getCrewId();
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));

        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        if (meetingCreateDto.getFeeType() == MeetingFeeType.PAID) {
            if (meetingCreateDto.getAccountNumber() == null && meetingCreateDto.getBankName() == null && meetingCreateDto.getAccountHolder() == null) {
                throw new IllegalArgumentException("유료 모임은 정산 계좌 정보가 필수입니다.");
            }
        }

        CrewMember crewMember = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("가입 승인된 크루원만 모임 생성이 가능합니다."));

        Meeting meeting = meetingCreateDto.toEntity(crew);
        // 사진 업로드 로직
        if (meetingCreateDto.getProfileImage() != null && !meetingCreateDto.getProfileImage().trim().isEmpty()) {
            meeting.updateMeetingImage(meetingCreateDto.getProfileImage());
        }
        // 드롭다운에서 선택한 좌표가 왔다면 그걸 사용 (TMAP 추가 호출 금지)
        if (meeting.getLatitude() != null && meeting.getLongitude() != null) {
            // 이미 좌표 세팅됨
        } else {
            // 좌표가 없을 때만 주소 지오코딩 시도
            TmapService.Coordinate c = tmapService.getCoordinateFromPlace(meeting.getPlace());
            meeting.updateLocation(c.getLatitude(), c.getLongitude());
        }
        meeting.addMemberCount(); // 모임장 자동 참여

        meeting = meetingRepository.save(meeting);

        MeetingMember meetingMember = MeetingMember.builder()
                .meeting(meeting)
                .crewMember(crewMember)
                .status(MeetingMemberStatus.APPROVED)
                .role(MeetingRole.OWNER)
                .build();

        meetingMemberRepository.save(meetingMember);

        return meeting.getId();
    }

    //    모임 목록 조회
    public Page<MeetingListDto> findAll(Long crewId, Pageable pageable) {
        Page<Meeting> meetingPage = meetingRepository.findAllByCrew_Id(crewId, pageable);
        return meetingPage.map(m -> MeetingListDto.fromEntity(m));
    }

    //    모임 상세 조회
    public MeetingDetailDto findById(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("없는 모임 입니다."));
        Crew crew = meeting.getCrew();
        if (crew == null || "Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루의 모임입니다.");
        }

        return MeetingDetailDto.fromEntity(meeting);
    }

    //   모임 수정(모임장만 가능)
    public void update(MeetingUpdateDto dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
//        해당 크루 소속 모임인지 확인
        Long crewId = dto.getCrewId();
        Long meetingId = dto.getMeetingId();
        Meeting meeting = meetingRepository.findByIdAndCrew_Id(meetingId, crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 모임 입니다."));
        MeetingMember meetingMember = meetingMemberRepository.findByMeeting_IdAndCrewMember_User_IdAndStatus(meetingId, user.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("모임 맴버가 아닙니다."));

        if (meetingMember.getRole() != MeetingRole.OWNER) {
            throw new IllegalStateException("모임 수정 권한이 없습니다.");
        }
        // 모임 시작 전만 수정 가능
        if (meeting.getMeetingAt() != null && meeting.getMeetingAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("이미 시작된 모임은 수정할 수 없습니다.");
        }
        String oldImageUrl = meeting.getProfileImage();
        String newImageUrl = dto.getProfileImage();

        if (newImageUrl == null || newImageUrl.trim().isEmpty()) {
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                try {
                    String oldFileName = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                    s3Client.deleteObject(a -> a.bucket(bucket).key(oldFileName));
                } catch (Exception e) {
                    System.out.println("기존 모임 이미지 삭제 실패: " + e.getMessage());
                }
            }
            meeting.updateMeetingImage(null); // DB를 null로 확실하게 비워줌!
        }
        else if (!newImageUrl.equals(oldImageUrl)) {
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                try {
                    String oldFileName = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                    s3Client.deleteObject(a -> a.bucket(bucket).key(oldFileName));
                } catch (Exception e) {
                    System.out.println("기존 모임 이미지 삭제 실패: " + e.getMessage());
                }
            }
            meeting.updateMeetingImage(newImageUrl); // 새 URL로 덮어쓰기
        }
        String beforePlace = meeting.getPlace();
        meeting.update(dto);

        String newPlace = meeting.getPlace();

        if (newPlace != null) newPlace = newPlace.trim();
        if (beforePlace != null) beforePlace = beforePlace.trim();

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            // 프론트에서 후보 선택한 좌표가 오면 그 좌표를 우선 사용
            meeting.updateLocation(dto.getLatitude(), dto.getLongitude());
        } else if (newPlace != null && !newPlace.equals(beforePlace)) {
            // 좌표가 없고 장소 문자열만 바뀐 경우에만 백엔드에서 지오코딩
            TmapService.Coordinate coordinate = tmapService.getCoordinateFromPlace(newPlace);
            meeting.updateLocation(coordinate.getLatitude(), coordinate.getLongitude());
        }


        // 일정 변경 알림: 승인된 모임원 전체에게 전송
        List<MeetingMember> members = meetingMemberRepository.findByMeeting_IdAndStatus(meetingId, MeetingMemberStatus.APPROVED);
        for (MeetingMember member : members) {
            notificationService.send(
                    member.getCrewMember().getUser(),
                    NotificationType.SCHEDULE_CHANGE,
                    "meeting",
                    meetingId,
                    "모임 일정이 변경되었습니다."
            );
        }
    }

    //  모임 삭제
    public void delete(Long meetingId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("없는 모임 입니다."));
        Crew crew = meeting.getCrew();
        if (crew == null || "Yes".equals(crew.getDelYn())) {
            throw new IllegalStateException("삭제된 크루의 모임은 삭제할 수 없습니다.");
        }
        MeetingMember meetingMember = meetingMemberRepository.findByMeeting_IdAndCrewMember_User_IdAndStatus(meetingId, user.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("모임 맴버가 아닙니다."));

        if (meetingMember.getRole() != MeetingRole.OWNER) {
            throw new IllegalStateException("모임 삭제 권한이 없습니다.");
        }
        meetingMemberRepository.deleteAllByMeeting_Id(meetingId);
        meetingRepository.delete(meeting);
    }

    //    모임상태 변경
//    OPEN/CLOSED/FINISHED
    public void changeStatus(MeetingStatusDto dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = dto.getCrewId();
        Long meetingId = dto.getMeetingId();
        Meeting meeting = meetingRepository.findByIdAndCrew_Id(meetingId, crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 모임입니다."));
        MeetingMember meetingMember = meetingMemberRepository.findByMeeting_IdAndCrewMember_User_IdAndStatus(meetingId, user.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("모임 맴버가 아닙니다."));
        if (meetingMember.getRole() != MeetingRole.OWNER) {
            throw new IllegalStateException("모임 상태변경 권한이 없습니다.");
        }

        meeting.updateStatus(dto.getStatus());
    }

    // 모임 종료할 때 출석체크(참석/노쇼)
    public void finishMeetingAndApplyAttendance(MeetingFinishedAttendanceDto dto) {
        Long crewId = dto.getCrewId();
        Long meetingId = dto.getMeetingId();

        // 노쇼 회원이 0명이면 그냥 빈 리스트 (전원 참석)
        List<Long> noShowIds = (dto.getNoShowMeetingMemberIds() == null) ? List.of() : dto.getNoShowMeetingMemberIds();

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 해당 크루 소속 모임인지 확인
        Meeting meeting = meetingRepository.findByIdAndCrew_Id(meetingId, crewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 크루의 모임이 아니거나 존재하지 않는 모임입니다."));

        //  모임장인지 확인
        MeetingMember meetingMember = meetingMemberRepository
                .findByMeeting_IdAndCrewMember_User_IdAndStatus(meetingId, user.getId(), MeetingMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("모임 맴버가 아닙니다."));

        if (meetingMember.getRole() != MeetingRole.OWNER) {
            throw new IllegalStateException("모임 종료 권한이 없습니다.");
        }

        // 이미 종료된 모임이면 또 처리 못 하게 막기
        if (meeting.getRecruitStatus() == MeetingRecruitStatus.FINISHED) {
            throw new IllegalStateException("이미 종료 처리된 모임입니다.");
        }
        // 모임 상태를 종료(FINISHED)로 바꾸기
        meeting.updateStatus(MeetingRecruitStatus.FINISHED);

        // 이 모임에 가입된 모임원들 전부 가져오기
        List<MeetingMember> members = meetingMemberRepository.findByMeeting_IdAndStatus(meetingId, MeetingMemberStatus.APPROVED);

        // 노쇼 리스트에 있으면 노쇼, 없으면 참석
        for (MeetingMember m : members) {
            User targetUser = m.getCrewMember().getUser();
            boolean isNoShow = noShowIds.contains(m.getId());

            EvaluationType type = isNoShow ? EvaluationType.NO_SHOW : EvaluationType.ATTEND;

            AttendanceRequestDto attendanceDto = AttendanceRequestDto.builder()
                    .crewId(meeting.getCrew().getId())
                    .meetingId(meeting.getId())
                    .targetUserId(targetUser.getId())
                    .type(type)
                    .build();
            mannerService.processAttendanceScore(attendanceDto);
        }
    }
//      마감 임박 모임 조회 (사이드바용, 최대 5개)
//       crewId  특정 크루 ID. null일 경우 전체 모임 대상
    @Cacheable(value = "incomingMeeting", key = "#email")
    public List<MeetingImminentDto> getImminentMeetings(Long crewId) {
        // 오늘부터 딱 3일(72시간) 이내로 다가온 모임 기준
        LocalDateTime deadline = LocalDateTime.now().plusDays(3);
        PageRequest limit = PageRequest.of(0, 5);

        List<Meeting> imminentMeetings = meetingRepository.findImminentMeetingsByCrew(crewId, deadline, limit);

        // 새 DTO로 변환
        return imminentMeetings.stream()
                .map(MeetingImminentDto::fromEntity)
                .collect(Collectors.toList());

    }

    // 내 모임 일정 조회
    @Transactional(readOnly = true)
    public List<MyMeetingListDto> findMyMeetings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 1. 현재 시간
        LocalDateTime now = LocalDateTime.now();
        // 2. 일주일(7일) 뒤 시간
        LocalDateTime oneWeekLater = now.plusDays(7);
        List<Meeting> meeting = meetingRepository
                .findMeetingsByUserIdAndStatusWithinOneWeek(user.getId(), MeetingMemberStatus.APPROVED, now, oneWeekLater);

        List<MyMeetingListDto> result = new ArrayList<>();
        for (Meeting m : meeting) {
            // 삭제된 크루의 모임 제외
            if (m.getCrew() != null
                    && !"Yes".equals(m.getCrew().getDelYn())) {
                result.add(MyMeetingListDto.fromEntity(m));
            }
        }
        return result;
    }
//    @Transactional(readOnly = true)
//    public Page<MeetingListDto> getCrewMeetings( Pageable pageable) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//
//        User user = userRepository.findAllByEmailWithAddress(email)
//                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
//        Page<Meeting> meetingPage = meetingRepository.findAllByJoinedCrews(
//                user.getId(),
//                CrewMemberStatus.APPROVED,
//                pageable);
//
//        return meetingPage.map(MeetingListDto::fromEntity);
//    }

    @Transactional(readOnly = true)
    public Page<MyMeetingListDto> findFinishedMyMeetings(Pageable pageable) {
        // 현재 로그인 유저 정보 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // Pageable에 'meeting.meetingAt' 경로로 정렬 조건 설정
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("meeting.meetingAt").descending()
        );

        Page<Meeting> meeting = meetingRepository
                .findFinishedMeetingsByUserId(user.getId(), MeetingMemberStatus.APPROVED, sortedPageable);

        return meeting.map(MyMeetingListDto::fromEntity);
    }
    public String getPresignedUrl(String originalFileName) {

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = "meeting" + "_" + System.currentTimeMillis() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // URL 유효시간 5분
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
}