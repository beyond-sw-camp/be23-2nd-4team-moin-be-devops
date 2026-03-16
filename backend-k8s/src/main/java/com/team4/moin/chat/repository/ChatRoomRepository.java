package com.team4.moin.chat.repository;

import com.team4.moin.chat.domain.ChatRoom;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByName(String roomName);

    List<ChatRoom> findAll(Specification<ChatRoom> specification);

    @Query("SELECT c FROM ChatRoom c WHERE c.name = :name AND c.isGroupChat = :isGroupChat AND c.delYn = 'N'")
    Optional<ChatRoom> findByNameAndIsGroupChat(@Param("name") String name, @Param("isGroupChat") String isGroupChat);

    @Query("SELECT c FROM ChatRoom c WHERE c.crew.id = :crewId AND c.delYn = 'N'")
    List<ChatRoom> findAllByCrewId(@Param("crewId") Long crewId);


}
