package com.team4.moin.feed.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedTextUpdateDto {
    private Long feedId;
    private String content;
}
