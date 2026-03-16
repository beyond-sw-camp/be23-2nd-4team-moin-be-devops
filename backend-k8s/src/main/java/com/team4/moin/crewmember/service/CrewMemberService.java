package com.team4.moin.crewmember.service;

import com.team4.moin.Notification.domain.NotificationType;
import com.team4.moin.Notification.service.NotificationService;
import com.team4.moin.chat.domain.ChatParticipant;
import com.team4.moin.chat.domain.ChatRoom;
import com.team4.moin.chat.repository.ChatRoomRepository;
import com.team4.moin.chat.service.ChatService;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import com.team4.moin.crewmember.domain.enums.CrewMemberStatus;
import com.team4.moin.crewmember.dtos.*;
import com.team4.moin.crewmember.repository.CrewMemberRepository;
import com.team4.moin.feed.domain.Feed;
import com.team4.moin.feed.repository.FeedRepository;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingRecruitStatus;
import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meeting.repository.MeetingRepository;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import com.team4.moin.meetingmember.repository.MeetingMemberRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CrewMemberService {
    private final CrewMemberRepository crewMemberRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final MeetingMemberRepository meetingMemberRepository;
    private final FeedRepository feedRepository;

    @Autowired
    public CrewMemberService(CrewMemberRepository crewMemberRepository, CrewRepository crewRepository, UserRepository userRepository, ChatRoomRepository chatRoomRepository, ChatService chatService, NotificationService notificationService, MeetingMemberRepository meetingMemberRepository,FeedRepository feedRepository) {
        this.crewMemberRepository = crewMemberRepository;
        this.crewRepository = crewRepository;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatService = chatService;
        this.notificationService = notificationService;
        this.meetingMemberRepository = meetingMemberRepository;
        this.feedRepository = feedRepository;
    }

    // 가입신청
    public Long crewJoin(CrewMemberReqDto crewMemberReqDto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = crewMemberReqDto.getCrewId();
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        // 크루 정원 확인
        if (crew.getMaxMembers() != null) {
            Integer approvedCount = crewMemberRepository.countByCrew_IdAndStatus(crewId, CrewMemberStatus.APPROVED);
            if (approvedCount >= crew.getMaxMembers()) {
                throw new IllegalStateException("크루 정원이 가득 찼습니다.");
            }
        }
        // 탈퇴했던 기존 크루멤버가 있으면 그 맴버를 가입대기 상태로 되돌림
        Optional<CrewMember> leftOpt = crewMemberRepository.findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.LEFT);
        if (leftOpt.isPresent()) {
            CrewMember leftMember = leftOpt.get();
            leftMember.updateStatus(CrewMemberStatus.PENDING);
            leftMember.updateJoinMessage(crewMemberReqDto.getJoinMessage());
            return leftMember.getId(); //
        }
        // 중복 신청 방지 (이미 가입신청후 대기중이거나 가입된 상태면 신청불가)
        if (crewMemberRepository.existsByCrew_IdAndUser_IdAndStatusIn(
                crewId, user.getId(), List.of(CrewMemberStatus.PENDING, CrewMemberStatus.APPROVED))) {
            throw new IllegalStateException("이미 가입 신청했거나 가입된 크루입니다.");
        }
        CrewMember leaderMember = crewMemberRepository
                .findByCrew_IdAndRole(crewId, CrewRole.OWNER)
                .orElseThrow(()->new EntityNotFoundException("크루장이 없습니다."));

        if (leaderMember != null) {
            notificationService.send(
                    leaderMember.getUser(),
                    NotificationType.APPLY,
                    "crew",
                    crewId,
                    user.getNickname() + "님이 " + crew.getName() + " 크루에 가입 신청했습니다."
            );
        }


//        신청내역 저장(가입대기상태)
        CrewMember join = CrewMember.builder()
                .crew(crew)
                .user(user)
                .status(CrewMemberStatus.PENDING)
                .joinMessage(crewMemberReqDto.getJoinMessage())
                .build();

        return crewMemberRepository.save(join).getId();
    }

    public List<CrewJoinRequestListDto> joinRequestList(Long crewId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }

        CrewRole role = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("해당 크루 멤버가 아닙니다."))
                .getRole();

        if (role != CrewRole.OWNER && role != CrewRole.MANAGER) {
            throw new IllegalArgumentException("가입 신청 목록 조회 권한이 없습니다.");
        }

        // 가입신청자 목록 조회
        List<CrewMember> pendingList =
                crewMemberRepository.findAllByCrewIdAndStatusWithUser(crewId, CrewMemberStatus.PENDING);

        List<CrewJoinRequestListDto> result = new ArrayList<>();
        for (CrewMember cm : pendingList) {
            result.add(CrewJoinRequestListDto.fromEntity(cm));
        }
        return result;
    }

    //    가입 승인
    public void approvedJoin(CrewMemberActionDto crewMemberActionDto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = crewMemberActionDto.getCrewId();
        Long joinId = crewMemberActionDto.getJoinId();
//        가입 승인 하는 사용자가 크루에 가입된 사용자인지
        CrewRole role = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("해당 크루 멤버가 아닙니다."))
                .getRole();
        //          크루 가입 승인 권한 확인
        if (role != CrewRole.OWNER && role != CrewRole.MANAGER) {
            throw new IllegalArgumentException("가입 승인 권한이 없습니다");
        }
        //        가입 신청 확인
        CrewMember join = crewMemberRepository.findByIdAndCrew_Id(joinId, crewId)
                .orElseThrow(() -> new EntityNotFoundException("가입 신청을 찾을 수 없습니다."));
        Crew crew = join.getCrew();
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }

//        가입상태 확인
        if (join.getStatus() != CrewMemberStatus.PENDING) {
            throw new IllegalStateException("가입 대기 중인 신청만 승인할 수 있습니다.");
        }
//        크루 정원 확인
        if (crew.getMaxMembers() != null &&
                crewMemberRepository.countByCrew_IdAndStatus(crewId, CrewMemberStatus.APPROVED) >= crew.getMaxMembers()) {
            throw new IllegalArgumentException("크루 정원이 다 찼습니다.");

        }
//        가입승인(가입상태,권한변경)
        join.updateStatus(CrewMemberStatus.APPROVED);
        join.updateRole(CrewRole.MEMBER);
//       가입승인시 크루 현재인원 +1


//       가입완료시 크루 현재인원 +1
        crew.addMemberCount();

//        가입 완료시, 크루채팅방에 추가
        ChatRoom chatRoom = chatRoomRepository.findByName(crew.getName()).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));
        chatService.addParticipantToGroupChat(chatRoom, join);

//        가입 완료시, 크루 가입 승인 알림 전송 (신청자에게 전달)
        notificationService.send(
                join.getUser(),
                NotificationType.APPROVE,
                "crew",
                crew.getId(),
                crew.getName()+
                "크루 신청이 승인되었습니다."
        );
    }

    //    가입거절
    public void rejectedJoin(CrewMemberActionDto crewMemberActionDto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = crewMemberActionDto.getCrewId();
        Long joinId = crewMemberActionDto.getJoinId();
        CrewRole role = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("해당 크루 멤버가 아닙니다."))
                .getRole();
        //          크루 가입 거절 권한 확인
        if (role != CrewRole.OWNER && role != CrewRole.MANAGER) {
            throw new IllegalArgumentException("가입 거절 권한이 없습니다");
        }
        //        가입 신청 확인
        CrewMember join = crewMemberRepository.findByIdAndCrew_Id(joinId, crewId)
                .orElseThrow(() -> new EntityNotFoundException("가입 신청을 찾을 수 없습니다."));

        Crew crew = join.getCrew();
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
//        가입상태 확인
        if (join.getStatus() != CrewMemberStatus.PENDING) {
            throw new IllegalStateException("가입 대기 중인 신청만 거절할 수 있습니다.");
        }
//        가입거절(가입상태 변경)
        join.updateStatus(CrewMemberStatus.REJECTED);

        // 가입 거절 알림 전송 (신청자에게 전달)
        notificationService.send(
                join.getUser(),
                NotificationType.REJECT,
                "crew",
                crew.getId(),
                "모임 신청이 거절되었습니다."
        );
    }

    //    크루 나가기
    public void leftCrew(CrewMemberLeftDto dto) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = dto.getCrewId();

        CrewMember crewMember = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("해당 크루 멤버가 아닙니다."));


        // 크루장(OWNER)인 경우: 크루원이 본인 혼자면 크루 삭제(소프트 딜리트), 아니면 차기 크루장 위임 필수
        if (crewMember.getRole() == CrewRole.OWNER) {

            //  현재 크루 승인 멤버 수 확인(본인 혼자인지 확인용)
            Integer approvedCount = crewMemberRepository.countByCrew_IdAndStatus(crewId, CrewMemberStatus.APPROVED);
            // 크루원이 본인 혼자라면 크루장으로 위임할 사람도 없으니 탈퇴가 아니라 크루 삭제
            if (approvedCount <= 1) {
                feedRepository.deleteByCrew_Id(crewId);
                chatService.leaveAllChatRoomsForCrewMember(crewMember);
                crewMember.updateStatus(CrewMemberStatus.LEFT);
                crewMember.getCrew().deleteCrew();
                return;
            }

            //  차기 크루장 지정 필수
            if (dto.getNextOwnerCrewMemberId() == null) {
                throw new IllegalArgumentException("크루장은 탈퇴 전 차기 크루장을 지정해야 합니다.");
            }
            // 차기 크루장이 이 크루에 있는지 확인
            CrewMember nextOwner = crewMemberRepository
                    .findByIdAndCrew_Id(dto.getNextOwnerCrewMemberId(), crewId)
                    .orElseThrow(() -> new IllegalStateException("차기 크루장은 해당 크루의 멤버여야 합니다."));
            // 차기 크루장이 승인된 멤버인지 확인
            if (nextOwner.getStatus() != CrewMemberStatus.APPROVED) {
                throw new IllegalStateException("차기 크루장은 승인된 멤버여야 합니다.");
            }
            // 본인을 차기 크루장으로 지정하는 실수 방지
            if (nextOwner.getId().equals(crewMember.getId())) {
                throw new IllegalArgumentException("본인을 차기 크루장으로 지정할 수 없습니다.");
            }

            // 크루장 위임
            crewMember.updateRole(CrewRole.MEMBER); //크루장 위임시 크루장이 2명이됨, 기존 크루장을 일반 맴버로 내리고 위임
            nextOwner.updateRole(CrewRole.OWNER);
        }

        // 참여중인 모든 모임에서 나가기 처리
        // 모임은 상태변경(LEFT) 안 하고, MeetingMember 기록을 하드삭제
        List<MeetingMember> meetingMembers = meetingMemberRepository.findAllByCrewMember_IdAndStatus(crewMember.getId(), MeetingMemberStatus.APPROVED);
        for (MeetingMember members : meetingMembers) {
            Meeting meeting = members.getMeeting();
            if (members.getRole() == MeetingRole.OWNER
                    && meeting.getRecruitStatus() != MeetingRecruitStatus.FINISHED) {
                meeting.updateStatus(MeetingRecruitStatus.FINISHED);
            }
            if (members.getStatus() == MeetingMemberStatus.APPROVED) {
                members.updateStatus(MeetingMemberStatus.LEFT);
                meeting.minusMemberCount();
            }
        }
        //내가 작성한 피드 삭제
        feedRepository.deleteByCrew_IdAndUser_Id(crewId, user.getId());
        //        탈퇴 처리 시, 해당 멤버 채팅 상태 변경
         chatService.leaveAllChatRoomsForCrewMember(crewMember);

        // 크루 탈퇴
        crewMember.updateStatus(CrewMemberStatus.LEFT);

//        크루 현재인원 감소
        crewMember.getCrew().minusMemberCount();
    }


    // 크루원 조회(승인된 멤버)
    public List<CrewMemberListDto> findCrewMembers(Long crewId) {
        // 현재 로그인 유저 email 추출 (isMe 판별용)
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));

        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        List<CrewMember> crewMembers = crewMemberRepository.findAllByCrewIdAndStatusWithUser(crewId, CrewMemberStatus.APPROVED);
        List<CrewMemberListDto> members = new ArrayList<>();
        for (CrewMember member : crewMembers) {
            members.add(CrewMemberListDto.fromEntity(member, currentEmail));
        }
        return members;
    }

    //    크루 운영진 조회
    public List<CrewMemberListDto> findCrewManagers(Long crewId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));

        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        List<CrewMember> crewManagers =
                crewMemberRepository.findAllByCrewIdAndStatusWithUser(crewId, CrewMemberStatus.APPROVED);
        List<CrewMemberListDto> managers = new ArrayList<>();
        for (CrewMember member : crewManagers) {
            if (member.getRole() == CrewRole.OWNER ||
                    member.getRole() == CrewRole.MANAGER) {
                managers.add(CrewMemberListDto.fromEntity(member));
            }
        }
        return managers;
    }

    //    권한 변경
    public void changeRole(CrewMemberRoleChangeDto dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        Long crewId = dto.getCrewId();

        CrewMember owner = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatus(crewId, user.getId(), CrewMemberStatus.APPROVED)
                .orElseThrow(() -> new IllegalStateException("해당 크루 멤버가 아닙니다."));

        if (owner.getRole() != CrewRole.OWNER) {
            throw new IllegalStateException("권한 변경은 크루장만 가능합니다.");
        }
        // 대상 멤버 조회
        CrewMember target = crewMemberRepository.findByIdAndCrew_Id(dto.getCrewMemberId(), crewId)
                .orElseThrow(() -> new EntityNotFoundException("대상 크루원을 찾을 수 없습니다."));

        // 대상이 승인된 멤버인지
        if (target.getStatus() != CrewMemberStatus.APPROVED) {
            throw new IllegalStateException("가입된 크루원만 권한 변경이 가능합니다.");
        }
        if (dto.getRole() == CrewRole.OWNER) {
            // 기존 크루장 내리기
            owner.updateRole(CrewRole.MEMBER);

            // 대상 크루장으로 승격
            target.updateRole(CrewRole.OWNER);
            return;
        }
        target.updateRole(dto.getRole());
    }

    public CrewMyStatusDto getMyStatus(Long crewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // PENDING 또는 APPROVED 상태의 크루멤버 조회
        Optional<CrewMember> crewMemberOpt = crewMemberRepository
                .findByCrew_IdAndUser_IdAndStatusIn(
                        crewId, user.getId(),
                        List.of(CrewMemberStatus.PENDING, CrewMemberStatus.APPROVED)
                );

        if (crewMemberOpt.isEmpty()) {
            return CrewMyStatusDto.builder()
                    .CrewMemberStatus("NONE")
                    .role(null)
                    .build();
        }

        CrewMember crewMember = crewMemberOpt.get();
        return CrewMyStatusDto.builder()
                .CrewMemberStatus(crewMember.getStatus().name())
                .role(crewMember.getRole() != null ? crewMember.getRole().name() : null)
                .build();
    }
    @Transactional(readOnly = true)
    public CrewMemberDetailDto getCrewMemberDetail(Long crewId, Long crewMemberId){
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        if (!crewMemberRepository.existsByCrewIdAndUserEmailAndStatus(crewId, currentEmail, CrewMemberStatus.APPROVED)) {
            throw new IllegalStateException("해당 크루의 멤버만 조회할 수 있습니다.");
        }
        CrewMember target = crewMemberRepository
                .findByIdAndCrew_IdWithUserAndCategories(crewMemberId, crewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 크루원을 찾을 수 없습니다."));

        if (target.getStatus() != CrewMemberStatus.APPROVED) {
            throw new IllegalStateException("승인된 크루원만 조회할 수 있습니다.");
        }

        return CrewMemberDetailDto.fromEntity(target, currentEmail);
    }

    @Transactional(readOnly = true)
    // 특정 크루원이 가입한 크루 목록 조회
    public List<CrewMemberJoinedCrewDto> getJoinedCrews(Long crewId, Long crewMemberId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();


        if (!crewMemberRepository.existsByCrewIdAndUserEmailAndStatus(crewId, currentEmail, CrewMemberStatus.APPROVED)) {
            throw new IllegalStateException("해당 크루의 멤버만 조회할 수 있습니다.");
        }
        CrewMember target = crewMemberRepository.findByIdAndCrew_Id(crewMemberId, crewId).orElseThrow(() -> new EntityNotFoundException("해당 크루원을 찾을 수 없습니다."));

        if (target.getStatus() != CrewMemberStatus.APPROVED) {
            throw new IllegalStateException("승인된 크루원만 조회할 수 있습니다.");
        }

        // 대상 유저가 APPROVED 상태로 가입한 크루 목록
        List<CrewMember> joinedCrewMembers = crewMemberRepository.findApprovedCrewsByUserId(target.getUser().getId());

        List<CrewMemberJoinedCrewDto> result = new ArrayList<>();
        for (CrewMember cm : joinedCrewMembers) {
            result.add(CrewMemberJoinedCrewDto.fromEntity(cm.getCrew()));
        }
        return result;
    }

}


