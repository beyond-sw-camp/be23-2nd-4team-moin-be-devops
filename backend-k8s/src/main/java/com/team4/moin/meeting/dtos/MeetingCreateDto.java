package com.team4.moin.meeting.dtos;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.MeetingFeeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingCreateDto {
    @NotNull
    private Long crewId;
    @NotBlank(message = "모임명 입력은 필수입니다.")
    private String meetingName;
    @NotBlank(message = "모임 장소 입력은 필수입니다.")
    private String place;
    private Integer maxMembers;
    @NotNull(message = "모임 일시 입력은 필수입니다.")
    private LocalDateTime meetingAt;
    @NotBlank(message = "모임 설명 입력은 필수입니다.")
    private String description;
    private MeetingFeeType feeType;
    private Long fee;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String profileImage;
    private Double latitude;
    private Double longitude;

    public Meeting toEntity(Crew crew) {
        Meeting meeting = Meeting.builder()
                .crew(crew)
                .name(this.meetingName)
                .place(this.place)
                .maxMembers(this.maxMembers)
                .meetingAt(this.meetingAt)
                .description(this.description)
                .bankName(this.bankName)
                .accountHolder(this.accountHolder)
                .accountNumber(this.accountNumber)
                .feeType(this.feeType)
                .fee(this.fee == null ? 0 : this.fee)
                .build();
        if (this.latitude != null && this.longitude != null) {
            meeting.updateLocation(this.latitude, this.longitude);
        }
        return meeting;
    }
}