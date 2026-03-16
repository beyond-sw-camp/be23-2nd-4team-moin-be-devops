package com.team4.moin.user.domain.entitys;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.common.domain.Category;
import com.team4.moin.user.domain.enums.Gender;
import com.team4.moin.user.domain.enums.Mbti;
import com.team4.moin.user.domain.enums.Provider;
import com.team4.moin.user.domain.enums.Role;
import com.team4.moin.user.dtos.UserUpdateDto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    private String nickname;
    private String profileImageUrl;
    @Column(precision = 3, scale = 1) // 3자리 중 소수점 1자리 ex) 00.0
    @Builder.Default
    private BigDecimal mannerScore = new BigDecimal("36.5"); // 36.5 디폴트
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;  // 관리자, 유저 권한 // 디폴트 user
    @Enumerated(EnumType.STRING)
    private Provider provider;// 소셜 로그인
    private String providerId; // 소셜 로그인 id
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Address address; // 케스케이드 올로 함께 삭제 및 등록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true) //, orphanRemoval = true 를 통해 clear()를 했을 때 User와 연결이 끊어진 카테고리 데이터들이 DB에서 자동으로 삭제
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
    private LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    private Mbti mbti;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Builder.Default
    private String delYn = "No";
    @Column(nullable = false)
    @Builder.Default
    private boolean isInfoCompleted = false; // 기본값 false


    public void createProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }
    public void updateAddress(Address address) {
        this.address = address;
    }

    public void updateAdditionalInfo(LocalDate birthDate, Gender gender, String nickname, Mbti mbti) {
        this.birthDate = birthDate;
        this.gender = gender;
        this.nickname = nickname;
        this.mbti = mbti;
        this.isInfoCompleted = true;
    }
    public void updateDataInfo(UserUpdateDto dto) {
        this.birthDate = dto.getBirthDate();
        this.gender = dto.getGender();
        this.nickname = dto.getNickname();
        this.mbti = dto.getMbti();
    }
    public void updateProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

    public void updateDelete() {
        this.delYn = "Yes";
        this.email = "deleted_"+ System.currentTimeMillis() + this.email; // email 앞에 deleted_ 붙여 추 후 동일 email로 회원가입 할 수 있음.
        this.providerId = "deleted_" + System.currentTimeMillis() + this.providerId;
    }
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
    public void updateMannerScore(BigDecimal delta) {
        if (delta == null) return;
        this.mannerScore = this.mannerScore.add(delta);
        //  0도 미만 방지
        if (this.mannerScore.compareTo(BigDecimal.ZERO) < 0) { // 0미만 방지
            this.mannerScore = BigDecimal.ZERO;
        }
    }
}
