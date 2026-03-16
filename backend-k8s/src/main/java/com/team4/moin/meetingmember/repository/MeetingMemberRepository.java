package com.team4.moin.meetingmember.repository;

import com.team4.moin.meeting.domain.enums.MeetingRole;
import com.team4.moin.meetingmember.domain.enums.MeetingMemberStatus;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import com.team4.moin.user.domain.entitys.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {
    Optional<MeetingMember> findByMeeting_IdAndCrewMember_User_IdAndStatus(Long meetingId, Long userId, MeetingMemberStatus status);
    //  모임 중복 가입신청 방지 (exists : 조건에 맞는 데이터가 하나라도 있는지 확인)
    boolean existsByMeeting_IdAndCrewMember_User_IdAndStatusIn(Long meetingId, Long userId, List<MeetingMemberStatus> statuses);
    //    이 모임에 신청한 가입이 맞는지 확인
    Optional<MeetingMember> findByIdAndMeeting_Id(Long id, Long meetingId);

    // 모임 삭제 전에 모임원 기록을 먼저 지우는 용도
    // meeting 삭제(하드딜리트)하면 FK 때문에 터질 수 있어서 meeting_member 먼저 삭제
    void deleteAllByMeeting_Id(Long meetingId);

    //  모임의 승인된 참가자 목록 (일정 변경 알림 등에 사용)
    List<MeetingMember> findByMeeting_IdAndStatus(Long meetingId, MeetingMemberStatus status);

    Optional<MeetingMember> findByMerchantUid(String merchantUid);


    @Query("select mm from MeetingMember mm " +
            "join fetch mm.crewMember cm " +
            "join fetch cm.user " + // 유저 정보(닉네임)까지 한 번에 가져옴
            "where mm.merchantUid = :merchantUid")
    Optional<MeetingMember> findByMerchantUidWithUser(@Param("merchantUid") String merchantUid);

    @Query("SELECT mm.crewMember.user FROM MeetingMember mm " +
            "WHERE mm.meeting.id = :meetingId AND mm.role = 'OWNER'")
    Optional<User> findOwnerByMeetingId(@Param("meetingId") Long meetingId);

    @Query("SELECT DISTINCT mm FROM MeetingMember mm " +
            "JOIN FETCH mm.meeting m " +
            "WHERE mm.crewMember.user.id = :userId " +
            "AND mm.status IN :statuses ")
    List<MeetingMember> findAllByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<MeetingMemberStatus> statuses);
    // 특정 모임(Meeting)에서 '환불 요청' 상태인 멤버들 조회
    List<MeetingMember> findAllByMeetingIdAndStatus(Long meetingId, MeetingMemberStatus status);

    // 특정 크루 멤버(CrewMember)의 환불/취소 내역 조회
    List<MeetingMember> findAllByCrewMemberIdAndStatusIn(Long crewMemberId, List<MeetingMemberStatus> statuses);
    @Query("SELECT COALESCE(SUM(m.meeting.fee), 0) FROM MeetingMember m " +
            "WHERE m.meeting.id = :meetingId AND m.status = :status")
    Long sumFeeByMeetingIdAndStatus(
            @Param("meetingId") Long meetingId,
            @Param("status") MeetingMemberStatus status
    );
//이 크루 맴버가 참여중인 모든 모임을 가져옴
    List<MeetingMember> findAllByCrewMember_IdAndStatus(
            Long crewMemberId, MeetingMemberStatus status);

// 이 크루 맴버가 참여중인 모임중에 모임장인 모임이 있는지 확인
    boolean existsByCrewMember_IdAndStatusAndRole(Long crewMemberId, MeetingMemberStatus status, MeetingRole role);
    // [수정] FETCH JOIN을 사용하여 Meeting 정보를 세션이 열려있을 때 한 번에 가져옴
    @Query("SELECT mm FROM MeetingMember mm " +
            "JOIN FETCH mm.meeting " +
            "JOIN FETCH mm.crewMember cm " +
            "JOIN FETCH cm.user " +
            "WHERE mm.id = :id")
    Optional<MeetingMember> findByIdWithMeetingAndUser(@Param("id") Long id);

    // 모임원 목록 조회용: crewMember/user/crew를 한 번에 fetch해서 N+1 방지
    @Query("""
    SELECT mm
    FROM MeetingMember mm
    JOIN FETCH mm.crewMember cm
    JOIN FETCH cm.user u
    JOIN FETCH cm.crew c
    WHERE mm.meeting.id = :meetingId
      AND mm.status = :status
""")
    List<MeetingMember> findByMeetingIdAndStatusWithFetch(
            @Param("meetingId") Long meetingId,
            @Param("status") MeetingMemberStatus status
    );



}
