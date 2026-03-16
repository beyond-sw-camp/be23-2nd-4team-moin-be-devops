package com.team4.moin.chat.repository;


import com.team4.moin.chat.domain.ChatParticipant;
import com.team4.moin.chat.domain.ChatParticipantStatus;
import com.team4.moin.chat.domain.ChatRoom;
import com.team4.moin.crew.domain.enums.CrewRole;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatRoomAndCrewMember(ChatRoom chatRoom, CrewMember crewMember);

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 " +
            "JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id " +
            "WHERE cp1.crewMember.user.id = :myUserId " +
            "AND cp2.crewMember.user.id = :otherUserId " +
            "AND cp1.chatRoom.isGroupChat = 'N' " +
            "AND cp1.chatRoom.delYn = 'N'")
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myUserId") Long myUserId,
                                               @Param("otherUserId") Long otherUserId);

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 " +
            "JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id " +
            "WHERE cp1.crewMember.user.id = :myUserId " +
            "AND cp2.crewMember.user.id = :otherUserId " +
            "AND cp1.chatRoom.isGroupChat = 'N' " +
            "AND cp1.chatRoom.delYn = 'N' " +
            "AND cp1.chatRoom.crew.id = :crewId") // ✅ 크루 조건 추가
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myUserId") Long myUserId,
                                               @Param("otherUserId") Long otherUserId,
                                               @Param("crewId") Long crewId);

    //    user에 카테고리 어드레스도 함꼐 fetch join
    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.crewMember cm JOIN FETCH cm.user WHERE cp.chatRoom = :chatRoom")
    List<ChatParticipant> findByChatRoomWithCrewMemberAndUser(@Param("chatRoom") ChatRoom chatRoom);

    // 특정 채팅방에 아직 남아있는 인원이 있는지 확인용
    List<ChatParticipant> findByChatRoomAndStatus(ChatRoom chatRoom, ChatParticipantStatus status);


    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.chatRoom cr " +
            "WHERE cp.crewMember = :crewMember AND cp.status = :status AND cr.delYn = 'N'")
    List<ChatParticipant> findAllByCrewMemberAndStatusWithChatRoom(@Param("crewMember") CrewMember crewMember,
                                                                   @Param("status") ChatParticipantStatus status);

    @Query("SELECT cm.role FROM ChatParticipant cp JOIN cp.crewMember cm JOIN cm.user u " +
            "WHERE cp.chatRoom.id = :roomId AND u.email = :email")
    Optional<CrewRole> findRoleByChatRoomIdAndUserEmail(@Param("roomId") Long roomId, @Param("email") String email);

    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.chatRoom cr JOIN FETCH cp.crewMember cm " +
            "JOIN FETCH cm.user u WHERE cr.id = :roomId AND u.email = :email")
    Optional<ChatParticipant> findByRoomIdAndUserEmail(@Param("roomId") Long roomId, @Param("email") String email);

//    // 크루 탈퇴 시, 해당 CrewMember가 속한 모든 채팅방 참여 정보 조회 (그룹 + 1:1)
//    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.chatRoom WHERE cp.crewMember = :crewMember "
//    + "AND cp.status = 'JOINED'")
//    List<ChatParticipant> findAllJoinedByCrewMember(@Param("crewMember") CrewMember crewMember);

    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.chatRoom WHERE cp.crewMember = :crewMember")
    List<ChatParticipant> findAllByCrewMember(@Param("crewMember") CrewMember crewMember);

    // 채팅방별 JOINED 인원 수 조회
    @Query("SELECT cp.chatRoom.id, COUNT(cp) FROM ChatParticipant cp WHERE cp.chatRoom.id IN :chatRoomIds " +
            "AND cp.status = 'JOINED' GROUP BY cp.chatRoom.id")
    List<Object[]> countMembersByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);

    // 1:1 채팅방에서 나를 제외한 상대방 조회 (닉네임, 프로필 이미지 fetch)
    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.crewMember cm JOIN FETCH cm.user u " +
            "WHERE cp.chatRoom = :chatRoom AND cp.crewMember != :me")
    Optional<ChatParticipant> findOpponentInPrivateRoom(@Param("chatRoom") ChatRoom chatRoom,
                                                        @Param("me") CrewMember me);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.crew.id = :crewId AND cr.delYn = 'N'")
    List<ChatRoom> findAllByCrewId(@Param("crewId") Long crewId);

}