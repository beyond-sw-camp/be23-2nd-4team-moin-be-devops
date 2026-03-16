package com.team4.moin.common.domain;

import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Category extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private User user;


}
