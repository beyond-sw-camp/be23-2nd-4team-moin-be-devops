package com.team4.moin.feed.dtos;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.feed.domain.Feed;
import com.team4.moin.feed.domain.FeedImage;
import com.team4.moin.user.domain.entitys.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedCreateDto {
    private Long crewId;
    private String content;
    @Builder.Default
    private List<String> images = new ArrayList<>(); // 최대 10장

    public Feed toEntity(User user, Crew crew) {
        return Feed.builder()
                .user(user)
                .crew(crew)
                .content(this.content)
                .build();
    }


}
