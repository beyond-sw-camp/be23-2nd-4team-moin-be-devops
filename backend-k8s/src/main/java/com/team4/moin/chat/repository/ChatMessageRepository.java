package com.team4.moin.chat.repository;

import com.team4.moin.chat.domain.ChatMessage;
import com.team4.moin.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

//    List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom chatRoom);

    // 전체 메시지 조회 (startMessageId가 null일 때)
    @Query("SELECT DISTINCT cm FROM ChatMessage cm JOIN FETCH cm.crewMember cmb JOIN FETCH cmb.user u " +
            "WHERE cm.chatRoom = :chatRoom ORDER BY cm.createdTime ASC")
    List<ChatMessage> findByChatRoomWithCrewMemberAndUser(@Param("chatRoom") ChatRoom chatRoom);

    // startMessageId 이후 메시지만 조회 (getChatHistory용)
    @Query("SELECT DISTINCT cm FROM ChatMessage cm JOIN FETCH cm.crewMember cmb JOIN FETCH cmb.user u " +
            "WHERE cm.chatRoom = :chatRoom AND cm.id > :startMessageId ORDER BY cm.createdTime ASC")
    List<ChatMessage> findByChatRoomWithCrewMemberAndUserAfterMessageId(@Param("chatRoom") ChatRoom chatRoom,
                                                                        @Param("startMessageId") Long startMessageId);
    Optional<ChatMessage> findByIdAndDelYn(Long roomId, String delYn);

    // 재가입 시 startMessageId 설정을 위한 최신 메시지 조회
    @Query("SELECT cm.id FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom ORDER BY cm.id DESC LIMIT 1")
    Optional<Long> findLatestMessageIdByChatRoom(@Param("chatRoom") ChatRoom chatRoom);

    // 최신 메시지 시간 + 내용 함께 조회
    @Query(value =
            "SELECT cm.chat_room_id, cm.content, cm.created_time FROM chat_message cm " +
                    "INNER JOIN (SELECT chat_room_id, MAX(created_time) AS max_time " +
                    "   FROM chat_message WHERE chat_room_id IN :chatRoomIds " +
                    "   GROUP BY chat_room_id) latest ON cm.chat_room_id = latest.chat_room_id AND cm.created_time = latest.max_time",
            nativeQuery = true)
    List<Object[]> findLastMessageByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);

}
