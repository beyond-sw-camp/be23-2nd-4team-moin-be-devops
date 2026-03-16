package com.team4.moin.chat.domain;

import com.team4.moin.chat.dtos.ChatMessageDto;
import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.crewmember.domain.entity.CrewMember;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class ChatMessage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crewMember_id", nullable = false)
    private CrewMember crewMember;

    @Column(nullable = false, length = 500)
    private String content;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<ReadStatus> readStatusList = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.NORMAL;

    @Builder.Default
    private String delYn = "N";

    @Builder.Default
    private String editedYn = "N"; // 수정 여부

    @ElementCollection
    @CollectionTable(name = "chat_message_images", joinColumns = @JoinColumn(name = "chat_message_id"))
    @Column(name = "image_url", length = 500)
    @BatchSize(size = 100)
    @Builder.Default
    private List<String> imgUrls = new ArrayList<>();

    public void delete(){
        this.delYn = "Y";
        this.content = "삭제된 메시지입니다.";
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.editedYn = "Y";

    }

}
