package com.team4.moin.crew.domain.entitys;

import com.team4.moin.common.domain.BaseTimeEntity;
import com.team4.moin.user.domain.entitys.User;

import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "crew_favorite",uniqueConstraints = {@UniqueConstraint(columnNames = {"crew_id", "user_id"})})
public class CrewFavorite extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Crew crew;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private User user;

}
