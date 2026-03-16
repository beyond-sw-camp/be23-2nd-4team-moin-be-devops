package com.team4.moin.user.domain.entitys;

import com.team4.moin.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String city;
    private String district;
    private String street;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private User user;

    public void updateAddress(String city, String district, String street) {
        this.city = city;
        this.district = district;
        this.street = street;
    }
}