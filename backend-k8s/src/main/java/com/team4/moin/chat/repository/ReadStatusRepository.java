package com.team4.moin.chat.repository;

import com.team4.moin.chat.domain.ChatMessage;
import com.team4.moin.chat.domain.ChatRoom;
import com.team4.moin.chat.domain.ReadStatus;
import com.team4.moin.chat.dtos.ChatMessageDto;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {


    // 안 읽은 메시지 수 조회 시 LEAVED 상태인 CrewMember 제외
    @Query("SELECT rs.chatMessage.id, COUNT(rs) FROM ReadStatus rs JOIN rs.crewMember cm " +
            "JOIN ChatParticipant cp ON cp.crewMember = cm AND cp.chatRoom = rs.chatRoom " +
            "WHERE rs.chatMessage.id IN :messageIds AND rs.isRead = :isRead " +
            "AND cp.status = 'JOINED' GROUP BY rs.chatMessage.id")
    List<Object[]> countByMessageIdsAndIsRead(@Param("messageIds") List<Long> messageIds,
                                              @Param("isRead") Boolean isRead);

    // 추가: countUnreadByRoomsAndCrewMembers - CrewMember 리스트
    @Query("SELECT rs.chatRoom.id, COUNT(rs) FROM ReadStatus rs " +
            "JOIN ChatParticipant cp ON cp.crewMember = rs.crewMember AND cp.chatRoom = rs.chatRoom " +
            "WHERE rs.chatRoom.id IN :chatRoomIds " +
            "AND rs.crewMember IN :crewMembers " +
            "AND rs.isRead = false " +
            "AND cp.status = 'JOINED' " +
            "GROUP BY rs.chatRoom.id")
    List<Object[]> countUnreadByRoomsAndCrewMembers(@Param("chatRoomIds") List<Long> chatRoomIds,
                                                    @Param("crewMembers") List<CrewMember> crewMembers);

    @Modifying
    @Transactional
    @Query(value = "UPDATE read_status SET is_read = true WHERE chat_room_id = :chatRoomId " +
            "AND crew_member_id = :crewMemberId AND is_read = false", nativeQuery = true)
    int bulkUpdateReadStatusNative(@Param("chatRoomId") Long chatRoomId, @Param("crewMemberId") Long crewMemberId);

    // 크루 탈퇴 시 해당 CrewMember의 ReadStatus 전체 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM ReadStatus rs WHERE rs.crewMember = :crewMember")
    void deleteAllByCrewMember(@Param("crewMember") CrewMember crewMember);

//    복귀한 참여자의 startMessageId 이전 메시지들의 ReadStatus
    @Query("UPDATE ReadStatus rs SET rs.isRead = true " +
            "WHERE rs.crewMember = :crewMember " +
            "AND rs.chatRoom = :chatRoom " +
            "AND rs.chatMessage.id <= :startMessageId")
    @Modifying
    void markAsReadUpTo(@Param("crewMember") CrewMember crewMember,
                        @Param("chatRoom") ChatRoom chatRoom,
                        @Param("startMessageId") Long startMessageId);

}
