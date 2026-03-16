package com.team4.moin.manner.dtos.manner;

import com.team4.moin.manner.domain.MannerLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MannerLogDto {
    private Long id;
    private String type;         // "GOOD", "BAD" 등 원본 문자열 이넘 두가지 사용 분기 어려워 스트링으로 반환
    private BigDecimal scoreDelta; // 매너 점수
    private LocalDateTime createdAt; // 시간

    public static MannerLogDto fromEntity(MannerLog log) {
        return MannerLogDto.builder()
                .id(log.getId())
                .type(log.getType())           // 가공 메서드 삭제 후 원본 주입
                .scoreDelta(log.getScoreDelta()) // 가공 메서드 삭제 후 원본 주입
                .createdAt(log.getCreatedTime()) // 엔티티의 LocalDateTime 그대로 주입
                .build();
    }
}