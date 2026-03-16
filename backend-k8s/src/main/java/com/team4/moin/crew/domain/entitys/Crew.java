package com.team4.moin.crew.domain.entitys;

import com.team4.moin.common.domain.BaseTimeEntity;


import com.team4.moin.crew.dtos.CrewUpdateDto;


import com.team4.moin.crew.service.ChosungUtil;
import com.team4.moin.user.domain.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Crew extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50)
    private String region;
    @Column(length = 50)
    private String district; // 구(강남구, 동작구 등)
    private String description;
    @Column(name = "crew_image", length = 255)
    private String crewImage;
    @Column(nullable = false)
    @Builder.Default
    private Integer currentMemberCount = 0;
    private Integer maxMembers;
    @Column(nullable = false)
    @Builder.Default
    private String delYn="No";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType categoryType;
    @Column(nullable = false)
    @Builder.Default
    private Integer favoriteCount = 0;
    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L; //크루 조회수
    // 평균(표시용)
    @Column(precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal ratingAvg = new BigDecimal("2.0");
    // 평가 개수
    @Builder.Default
    private Long ratingCount = 0L;
    // 총합(오차 방지)
    @Column(precision = 10, scale = 1)
    @Builder.Default
    private BigDecimal ratingTotal = BigDecimal.ZERO;


    @Column(name = "chosung")
    private String chosung; // 🔥 초성을 저장할 컬럼 추가



    public void updateCrewImage(String imageUrl) {
        this.crewImage = imageUrl;
    }
    public void deleteCrew(){
        this.delYn="Yes";

    }
    public void updateCrew(CrewUpdateDto dto){
        if (dto.getCrewName() != null ) this.name = dto.getCrewName();
        if (dto.getRegion() != null) this.region = dto.getRegion();
        if (dto.getDistrict() != null) this.district = dto.getDistrict();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getMaxMembers() != null) this.maxMembers = dto.getMaxMembers();
        if (dto.getCategoryType() != null) this.categoryType = dto.getCategoryType();
        if (dto.getCrewImage() != null) this.crewImage = dto.getCrewImage();
    }

//    크루 가입/탈퇴 시 현재인원 증감
    public void addMemberCount() {
        this.currentMemberCount++;
    }

    public void minusMemberCount() {
        if (this.currentMemberCount > 0) {
            this.currentMemberCount--;
        }
    }
//    크루 평점 계산
public void applyRating(BigDecimal score) {
    this.ratingTotal = this.ratingTotal.add(score);                 // 총합 +
    this.ratingCount = this.ratingCount + 1;                        // 개수 +
    BigDecimal totalWithBase = this.ratingTotal.add(new BigDecimal("2.0").multiply(BigDecimal.valueOf(2)));
    long countWithBase = this.ratingCount + 2;
    this.ratingAvg = totalWithBase.divide(BigDecimal.valueOf(countWithBase), 1, RoundingMode.HALF_UP); //평균
}

    // DB에 Insert 되거나 Update 되기 직전에 자동으로 초성을 계산해서 채워 넣습니다.
    @PrePersist
    @PreUpdate
    public void generateChosung() {
        if (this.name != null) {
            this.chosung = ChosungUtil.extractChosung(this.name);
        }
    }
}
