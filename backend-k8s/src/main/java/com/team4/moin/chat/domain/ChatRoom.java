package com.team4.moin.chat.domain;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.crew.domain.entitys.Crew;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String chatRoomImage;

    @Builder.Default
    private String isGroupChat ="N"; // 그룹채팅인가

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<ChatParticipant> chatParticipantList = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = true)
    private Crew crew;

    @Builder.Default
    private String delYn="N";

    public void deleteChatRoom(){
        this.delYn="Y";
    }

    public void updateChatRoomImage(String imageUrl) {
        this.chatRoomImage = imageUrl;
    }

}
