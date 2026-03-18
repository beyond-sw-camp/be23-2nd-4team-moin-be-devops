package com.team4.moin.crew.service;


import com.team4.moin.chat.repository.ChatRoomRepository;
import com.team4.moin.crew.domain.entitys.CrewFavorite;
import com.team4.moin.crew.dtos.*;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.chat.service.ChatService;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crew.repository.CrewFavoriteRepository;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.feed.repository.FeedRepository;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.meetingmember.repository.MeetingMemberRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@Transactional
public class CrewService {
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewFavoriteRepository crewFavoriteRepository;
    private final S3Client s3Client;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final FeedRepository feedRepository;
    private final S3Presigner s3Presigner;
    private final RedisTemplate<String, String> crewFavoriteRedis; // 찜
    private final RedisTemplate<String, String> crewViewRedis;     // 조회수
    private final CrewCacheService crewCacheService;
    private final ChatRoomRepository chatRoomRepository;


    @Value("${aws.s3.bucket4}")
    private String bucket;

    @Autowired
    public CrewService(CrewRepository crewRepository, CrewMemberRepository crewMemberRepository, CrewFavoriteRepository crewFavoriteRepository, S3Client s3Client, UserRepository userRepository, ChatService chatService, MeetingRepository meetingRepository, MeetingMemberRepository meetingMemberRepository, FeedRepository feedRepository, S3Presigner s3Presigner, @Qualifier("crewFavoriteInventory") RedisTemplate<String, String> crewFavoriteRedis, @Qualifier("crewViewInventory") RedisTemplate<String, String> crewViewRedis, CrewCacheService crewCacheService, ChatRoomRepository chatRoomRepository)  {
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewFavoriteRepository = crewFavoriteRepository;
        this.s3Client = s3Client;
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.meetingRepository = meetingRepository;
        this.meetingMemberRepository = meetingMemberRepository;

        this.feedRepository = feedRepository;
        this.s3Presigner = s3Presigner;
        this.crewFavoriteRedis = crewFavoriteRedis;
        this.crewViewRedis = crewViewRedis;
        this.crewCacheService = crewCacheService;
        this.chatRoomRepository = chatRoomRepository;
    }

    //    크루생성
    public Long save(CrewCreateDto crewCreateDto) {
//     로그인,유저 email 가져오기
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
//      userid꺼내기
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
//       크루 저장
        Crew crew = crewCreateDto.toEntity();



//               크루사진
        if (crewCreateDto.getCrewImage() != null && !crewCreateDto.getCrewImage().trim().isEmpty()) {
            crew.updateCrewImage(crewCreateDto.getCrewImage());
        }
        crewRepository.save(crew);


        //        크루 생성자 크루장으로(가입상태도 변경)
        CrewMember crewMember = CrewMember.builder()
                .crew(crew)
                .user(user)
                .status(CrewMemberStatus.APPROVED)
                .role(CrewRole.OWNER)
                .build();
        crewMemberRepository.save(crewMember);
//        크루 생성시 크루장 가입(현재인원 +1)
        crew.addMemberCount();
//        크루채팅방생성
        chatService.createGroupRoom(crewCreateDto.getCrewName(), crew);
        return crew.getId();
    }

    //    크루목록조회/검색
    public Page<CrewListDto> findAll(Pageable pageable, CrewSearchDto crewSearchDto) {
        Specification<Crew> specification = new Specification<Crew>() {
            @Override
            public Predicate toPredicate(Root<Crew> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(criteriaBuilder.equal(root.get("delYn"), "No"));
//                지역 검색
                if (crewSearchDto.getRegion() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("region"), crewSearchDto.getRegion()));
                }
                // 구(district) 검색
                if (crewSearchDto.getDistrict() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("district"), crewSearchDto.getDistrict()));
                }
//                카테고리 검색
                if (crewSearchDto.getCategoryTypes() != null) {
                    predicateList.add(root.get("categoryType").in(crewSearchDto.getCategoryTypes()));
                } else if (crewSearchDto.getCategoryType() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("categoryType"), crewSearchDto.getCategoryType()));
                }
                // 크루명 검색 (부분검색)
                if (crewSearchDto.getCrewName() != null) {
                    predicateList.add(criteriaBuilder.like((root.get("name")), "%" + crewSearchDto.getCrewName() + "%"));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateArr.length; i++) {
                    predicateArr[i] = predicateList.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Crew> crewPage = crewRepository.findAll(specification, pageable);
        Page<CrewListDto> dtoPage = crewPage.map(crew -> CrewListDto.fromEntity(crew));

        //  로그인 여부 확인
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        // 이메일로 유저 조회 (비로그인이면 user=null)
        User user = userRepository.findAllByEmail(email).orElse(null);
        if (user == null) { // 비로그인이면 찜 표시 안함
            return dtoPage;
        }
        Long userId = user.getId(); // 로그인 유저 id
//        유저id로 레디스 키 만듦(해당유저가 찜한 크루 목록)
        String favKey = "crew:fav:user:" + userId;
        // 레디스에 해당 유저가 찜한 크루 목록 존재 여부 확인,없으면 db에서 가져옴
        if (!Boolean.TRUE.equals(crewFavoriteRedis.hasKey(favKey))) {
            List<CrewFavorite> favorites = crewFavoriteRepository.findAllByUserIdWithCrew(userId); // db에서 해당유저 찜 목록
//            db에 찜한 크루가 1개라도 있으면
            if (favorites != null && !favorites.isEmpty()) {
                String[] ids = favorites.stream()
                        .map(cf -> cf.getCrew().getId().toString()) // 크루id만 뽑기
                        .toArray(String[]::new);
                crewFavoriteRedis.opsForSet().add(favKey, ids); // 레디스에 크루id들 넣음
            }
            // 찜한 크루가 1개도 없으면 레디스에 굳이 넣을 것도 없으니 넘어감
        }
//        db에서 받아온 크루 목록에서 이번 페이지에 보여줄 크루들 리스트 꺼냄
        List<CrewListDto> content = dtoPage.getContent();
        // 파이프라인: "찜했냐?" 질문을 여러개(페이지 크루 수만큼) 만들어서 한번에 레디스로 보냄
        List<Object> results = crewFavoriteRedis.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                // 이번 페이지 크루들 하나씩 보면서 "이 크루id가 해당유저 찜목록에 있냐?"를 계속 요청으로 쌓음
                for (CrewListDto dto : content) {
                    operations.opsForSet().isMember(favKey, dto.getCrewId().toString());// 질문을 쌓아두기
                }
                return null;
            }
        });
        // results 순서대로 true/false가 들어오니 DTO에 그대로 넣기
        for (int i = 0; i < content.size(); i++) {
            content.get(i).setFavorite(Boolean.TRUE.equals(results.get(i))); // 찜 여부 세팅
        }
        return dtoPage;
    }
    //    인기순 크루 조회
    public List<CrewListDto> findPopularCrew(Pageable pageable) {

        List<CrewListDto> result = crewCacheService.getPopularCrewsCache();


        Page<Crew> crewPage = crewRepository.findAllByDelYn("No", pageable);

        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findAllByEmail(email).orElse(null);
        if (user == null) {
            return result;
        }

        Long userId = user.getId();
        String favKey = "crew:fav:user:" + userId;
        if (!Boolean.TRUE.equals(crewFavoriteRedis.hasKey(favKey))) {
            List<CrewFavorite> favorites = crewFavoriteRepository.findAllByUserIdWithCrew(userId);
            if (favorites != null && !favorites.isEmpty()) {
                String[] ids = favorites.stream()
                        .map(cf -> cf.getCrew().getId().toString())
                        .toArray(String[]::new);
                crewFavoriteRedis.opsForSet().add(favKey, ids);
            }
        }
        List<Object> results = crewFavoriteRedis.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                for (CrewListDto dto : result) {
                    operations.opsForSet().isMember(favKey, dto.getCrewId().toString());
                }
                return null;
            }
        });
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setFavorite(Boolean.TRUE.equals(results.get(i)));
        }
        return result;
    }
    //    크루 상세 조회
    public CrewDetailDto findById(Long id, HttpServletRequest request) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email).orElse(null);
        //  찜 여부 확인(기본값 false)
        boolean isFav = false;
        // 로그인한 경우에만 찜 여부 확인
        if (user != null) {
            Long userId = user.getId(); // 로그인 유저 id

            // 유저id로 레디스 키 만듦(해당유저가 찜한 크루 목록)
            String favKey = "crew:fav:user:" + userId;
            // 세트 안에 들어갈 값(지금 보고있는 크루 id)
            String member = id.toString();
            // 레디스에 찜 목록이 있으면 레디스에서 바로 확인
            if (Boolean.TRUE.equals(crewFavoriteRedis.hasKey(favKey))) {
                isFav = Boolean.TRUE.equals(crewFavoriteRedis.opsForSet().isMember(favKey, member));
            }
            // 레디스에 찜 목록이 없으면 db에서 내가 찜한 크루들을 가져와서 레디스에 통째로 넣음
            else {
                List<CrewFavorite> favorites = crewFavoriteRepository.findAllByUserIdWithCrew(userId); // db에서 해당유저 찜 목록

                // db에 찜한 크루가 1개라도 있으면
                if (favorites != null && !favorites.isEmpty()) {
                    String[] ids = favorites.stream()
                            .map(cf -> cf.getCrew().getId().toString()) // 크루id만 뽑기
                            .toArray(String[]::new);
                    crewFavoriteRedis.opsForSet().add(favKey, ids); // 레디스에 크루id들을 넣음
                    // 방금 db에서 뽑은 전체 목록 안에 현재 크루 id가 있으면 찜한 크루
                    Set<String> favSet = new HashSet<>(Arrays.asList(ids));
                    isFav = favSet.contains(member);
                }
                // 찜한 크루가 1개도 없으면 false 그대로
            }
        }
        // 비로그인이면 isFav는 false 그대로

        // 조회수 24시간 1번만 올리기(로그인: userId / 비로그인: IP로 구분)
        String viewerKey; // 누가 봤는지 구분
        if (user != null) {
            viewerKey = "U" + user.getId(); // 로그인: 유저ID로 고정
        } else {
            viewerKey = "IP" + getClientIp(request); // 비로그인: IP로 구분
        }
        // 크루ID + 보는사람(유저/아이피) 조합으로 24시간짜리 키 생성
        String viewKey = "CREW_VIEW:" + id + ":" + viewerKey;
        // 이 키가 "없을때만" 저장 성공(true) + 24시간 뒤 자동삭제
        Boolean first = crewViewRedis.opsForValue().setIfAbsent(viewKey, "1", 24, TimeUnit.HOURS);
        // 처음 보는 거면 조회수 +1
        if (Boolean.TRUE.equals(first)) {
            crewRepository.incrementViewCount(id);
        }
        // 조회수 반영된 값까지 포함해서 다시 가져오기(최종 응답용)
        Crew updatedCrew = crewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));
        CrewDetailDto dto = CrewDetailDto.fromEntity(updatedCrew);
        // 찜 여부 넣기
        dto.setFavorite(isFav);
        return dto;
    }
    // 비로그인 사용자 ip
    private String getClientIp(HttpServletRequest request) {
        // 중간 서버가 사용자 IP를 적어주는 메모(헤더)가 있으면 먼저 꺼냄
        String xff = request.getHeader("X-Forwarded-For");
        // 메모가 있으면 여러 개가 올 수 있어서 첫 번째만 사용
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        // 메모가 없으면 그냥 지금 요청을 보낸 곳의 IP를 사용
        return request.getRemoteAddr();
    }

    //    크루삭제
    public void delete(Long crewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Crew crew = crewRepository.findByIdForUpdate(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));

        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("이미 삭제된 크루 입니다.");
        }
        CrewRole role = crewMemberRepository.findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("크루 멤버가 아닙니다."))
                .getRole();

        if (role != CrewRole.OWNER) {
            throw new IllegalArgumentException("크루 삭제 권한이 없습니다.");
        }
        // 크루에 속한 모임들 조회
        List<Meeting> meetings = meetingRepository.findAllByCrew_Id(crewId, Pageable.unpaged()).getContent();
        for (Meeting meeting : meetings) {
            Long meetingId = meeting.getId();
            // 모임 참가자 먼저 삭제 (FK 보호)
            meetingMemberRepository.deleteAllByMeeting_Id(meetingId);
            //  모임 삭제 (하드 딜리트)
            meetingRepository.delete(meeting);
        }
        //  크루 찜 삭제
        crewFavoriteRepository.deleteAllByCrew_Id(crewId);
//      크루 그룹 채팅방 삭제
        chatService.deleteAllChatRoomsByCrew(crew.getId());
        //  크루 전체 피드 삭제
        feedRepository.deleteByCrew_Id(crewId);
        //  크루 삭제 (소프트 딜리트)
        crew.deleteCrew();

    }

    // 크루수정
    public void updateCrew(CrewUpdateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = dto.getCrewId();
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        CrewRole role = crewMemberRepository.findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("크루 멤버가 아닙니다."))
                .getRole();

        if (role != CrewRole.OWNER && role != CrewRole.MANAGER) {
            throw new IllegalArgumentException("크루 수정 권한이 없습니다.");
        }
        String oldImageUrl = crew.getCrewImage();
        String newImageUrl = dto.getCrewImage();

        if (newImageUrl == null || newImageUrl.trim().isEmpty()) {
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                try {
                    String oldFileName = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                    s3Client.deleteObject(a -> a.bucket(bucket).key(oldFileName));
                } catch (Exception e) {
                    System.out.println("기존 크루 이미지 삭제 실패: " + e.getMessage());
                }
            }
            crew.updateCrewImage(null); // DB를 null로 확실하게 비워줌!

            chatRoomRepository.findByNameAndIsGroupChat(crew.getName(), "Y")
                    .ifPresent(chatRoom -> chatRoom.updateChatRoomImage(newImageUrl));
        }
        else if (!newImageUrl.equals(oldImageUrl)) {
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                try {
                    String oldFileName = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                    s3Client.deleteObject(a -> a.bucket(bucket).key(oldFileName));
                } catch (Exception e) {
                    System.out.println("기존 크루 이미지 삭제 실패: " + e.getMessage());
                }
            }
            crew.updateCrewImage(newImageUrl); // 새 URL로 덮어쓰기

//            크루 채팅방 이미지 변경
            chatRoomRepository.findByNameAndIsGroupChat(crew.getName(), "Y")
                    .ifPresent(chatRoom -> chatRoom.updateChatRoomImage(newImageUrl));
        }

        // 기본 정보 수정
        crew.updateCrew(dto);
    }

    //    크루 평점
    public BigDecimal rateCrew(CrewRatingDto dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Crew crew = crewRepository.findById(dto.getCrewId())
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        CrewMember crewMember = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(dto.getCrewId(), user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("크루 멤버가 아닙니다."));
        BigDecimal score = dto.getScore();
        // 평점 범위  (1.0 ~ 5.0)
        if (score.compareTo(BigDecimal.ONE) < 0 ||
                score.compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("평점은 1.0~5.0 사이여야 합니다.");
        }
        if (crewMember.getCrewRating() != null) {
            throw new IllegalStateException("이미 평점을 남겼습니다.");
        }
        crewMember.updateCrewRating(dto.getScore());
        crew.applyRating(score);
        return crew.getRatingAvg();
    }


    // 페이징은 캐시 위에서 처리 (캐시 없음)
//    맞춤 추천 크루 캐싱 + 페이징 처리
    public Page<CrewListDto> getRecommendedCrewsPage(Pageable pageable, String email) {
        List<CrewListDto> cached = crewCacheService.getRecommendedCrewsCache(email);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), cached.size());
        return new PageImpl<>(cached.subList(start, end), pageable, cached.size());
    }


//    급상승 인기 크루
    public Page<CrewListDto> getRookieCrewsPage(Pageable pageable, String email) {
        List<CrewListDto> cached = crewCacheService.getSoaringCrewsCache(email);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), cached.size());
        return new PageImpl<>(cached.subList(start, end), pageable, cached.size());
    }

    // 내가 가입한 크루 목록 조회
    public List<MyCrewListDto> findMyCrew() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        List<CrewMember> crewMembers = crewMemberRepository.findAllByUserEmail(email);

        List<MyCrewListDto> result = new ArrayList<>();
        for (CrewMember cm : crewMembers) {
            // 삭제된 크루 제외, 승인된 멤버만
            if (cm.getStatus() == CrewMemberStatus.APPROVED
                    && !"Yes".equals(cm.getCrew().getDelYn())) {
                result.add(MyCrewListDto.fromEntity(cm));
            }
        }
        return result;
    }

    public List<CrewSuggestResponseDto> getCrewSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalized = keyword.trim();
        boolean chosungOnly = normalized.matches("^[ㄱ-ㅎ]+$");

        List<Crew> crews = chosungOnly
                ? crewRepository.findByChosungKeyword(normalized, PageRequest.of(0, 5))
                : crewRepository.findByNameKeyword(normalized, PageRequest.of(0, 5));

        return crews.stream()
                .map(CrewSuggestResponseDto::fromEntity)
                .toList();
    }
    // 1. 참여 대기 중인 크루 목록 (페이징)
    public Page<MyCrewListDto> getPendingCrews(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        return crewMemberRepository.findByUser_IdAndStatusAndCrew_DelYn(
                user.getId(), CrewMemberStatus.PENDING, "No", pageable
        ).map(MyCrewListDto::fromEntity);
    }

    // 2. 참여 중인 크루 목록 (운영자 제외, 일반 멤버/매니저만 - 페이징)
    public Page<MyCrewListDto> getParticipatingCrews(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // CrewRole.OWNER가 아닌(Not) APPROVED 상태의 멤버십만 가져옴
        return crewMemberRepository.findByUser_IdAndStatusAndRoleNotAndCrew_DelYn(
                user.getId(), CrewMemberStatus.APPROVED, CrewRole.OWNER, "No", pageable
        ).map(MyCrewListDto::fromEntity);
    }

    // 3. 운영 중인 크루 목록 (크루장 - 페이징)
    public Page<MyCrewListDto> getLeadingCrews(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        return crewMemberRepository.findByUser_IdAndStatusAndRoleAndCrew_DelYn(
                user.getId(), CrewMemberStatus.APPROVED, CrewRole.OWNER, "No", pageable
        ).map(MyCrewListDto::fromEntity);
    }

    // 4. 찜한 크루 목록 (페이징)
    public Page<CrewListDto> getFavoriteCrews(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 찜한 크루는 MyCrewListDto보다 일반 CrewListDto가 어울립니다.
        return crewFavoriteRepository.findByUser_IdAndCrew_DelYn(
                user.getId(), "No", pageable
        ).map(cf -> {
            CrewListDto dto = CrewListDto.fromEntity(cf.getCrew());
            dto.setFavorite(true); // 찜한 목록이므로 무조건 true
            return dto;
        });
    }
    public String getPresignedUrl(String originalFileName) {

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = "crew" + "_" + System.currentTimeMillis() + extension;

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



