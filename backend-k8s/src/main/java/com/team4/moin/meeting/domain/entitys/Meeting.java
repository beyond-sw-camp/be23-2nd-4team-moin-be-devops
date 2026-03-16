package com.team4.moin.meeting.domain.entitys;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.meeting.domain.enums.MeetingFeeType;
import com.team4.moin.meeting.domain.enums.MeetingRecruitStatus;
import com.team4.moin.meeting.dtos.MeetingUpdateDto;
import com.team4.moin.meetingmember.domain.entity.MeetingMember;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Crew crew;
    @Column(nullable = false, length = 100)
    private String name;
    // 모임 일시
    @Column(nullable = false)
    private LocalDateTime meetingAt;
    // 모임장소
    @Column(nullable = false, length = 100)
    private String place;
    private String description;
    @Column(nullable = false)
    @Builder.Default
    private Integer currentMemberCount = 0;
    private Integer maxMembers;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingRecruitStatus recruitStatus = MeetingRecruitStatus.OPEN;
    @Enumerated(EnumType.STRING)
    private MeetingFeeType feeType;
    private Long fee;
    @Builder.Default
    private Long totalFeePool = 0L; // 모인 회비 총액
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    @Column(nullable = false)
    @Builder.Default
    private boolean isSettled = false;
    private String profileImage;
    @OneToMany(mappedBy = "meeting")
    @Builder.Default
    private List<MeetingMember> meetingMembers = new ArrayList<>();

    //    지도 좌표
    private Double latitude;
    private Double longitude;

    public void updateLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public void markAsSettled() {
        this.isSettled = true;
    }

    //    모임 가입/탈퇴 시 현재인원 증감
    public void addMemberCount() {
        if (this.maxMembers != null && this.currentMemberCount >= this.maxMembers) {
            throw new IllegalStateException("모임 정원이 가득 찼습니다.");
        }
        this.currentMemberCount++;
    }

    public void minusMemberCount() {
        if (this.currentMemberCount > 0) {
            this.currentMemberCount--;
        }
    }

    public void update(MeetingUpdateDto dto) {
        if (dto.getMeetingName() != null) this.name = dto.getMeetingName();
        if (dto.getPlace() != null) this.place = dto.getPlace();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getMeetingAt() != null) this.meetingAt = dto.getMeetingAt();
        if (dto.getMaxMembers() != null) this.maxMembers = dto.getMaxMembers();
        if (dto.getFee() != null) this.fee = dto.getFee();
        if (dto.getBankName() != null) this.bankName = dto.getBankName();
        if (dto.getAccountNumber() != null) this.accountNumber = dto.getAccountNumber();
        if (dto.getAccountHolder() != null) this.accountHolder = dto.getAccountHolder();
    }

    public void updateStatus(MeetingRecruitStatus status) {
        this.recruitStatus = status;
    }

    public boolean isPaid() {
        return this.feeType == MeetingFeeType.PAID;
    }

    // 결제 전용 고유 식별자 생성 로직 (포트원 전송용)
    public String createMerchantUid(Long userId) {
        return "MEET_" + this.id + "_" + userId + "_" + System.currentTimeMillis();
    }

    // 회비 추가 메서드
    public void addFee(Long amount) {
        if (this.totalFeePool == null) this.totalFeePool = 0L;
        this.totalFeePool += amount;
    }

    public boolean isFull() {
        return this.currentMemberCount >= this.maxMembers;
    }
    public void minusFee(Long amount) {
        if (this.totalFeePool != null && this.totalFeePool >= amount) {
            this.totalFeePool -= amount;
        }
    }
    public void updateMeetingImage(String imageUrl){
        this.profileImage = imageUrl;
    }

}
