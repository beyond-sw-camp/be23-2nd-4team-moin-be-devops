package com.team4.moin.crew.dtos;

import com.team4.moin.crew.domain.entitys.Crew;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewSuggestResponseDto {

    private Long id;
    private String name;

    // 💡 프론트엔드 드롭다운을 더 예쁘게 만들고 싶다면 이미지 URL도 추가해서 내려주는 것을 추천합니다!
    // private String imageUrl;

    public static CrewSuggestResponseDto fromEntity(Crew crew) {
        return CrewSuggestResponseDto.builder()
                .id(crew.getId())
                .name(crew.getName())
                // .imageUrl(crew.getCrewImage()) // 크루 썸네일 필드가 있다면 주석 해제해서 같이 맵핑해 주세요
                .build();
    }
}
