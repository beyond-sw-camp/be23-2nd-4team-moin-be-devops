package com.team4.moin.report.repository;

import com.team4.moin.report.domain.Report;
import com.team4.moin.report.domain.ReportStatus;
import com.team4.moin.report.domain.ReportTargetType;
import com.team4.moin.report.dtos.ReportCountDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 중복 신고 방지
    Optional<Report> findByUserIdAndTargetTypeAndTargetId(
            Long userId,
            ReportTargetType targetType,
            Long targetId
    );

    List<Report> findAllByStatus(ReportStatus status);

    @Query("SELECT new com.team4.moin.report.dtos.ReportCountDto(r.targetId, COUNT(r)) " +
            "FROM Report r WHERE r.targetId IN :targetIds GROUP BY r.targetId")
    List<ReportCountDto> countByTargetIds(@Param("targetIds") List<Long> targetIds);
    long countByTargetId(Long targetId);
    long countByStatus(ReportStatus status);
    @Query("SELECT new com.team4.moin.report.dtos.ReportCountDto(r.targetId, COUNT(r)) " +
            "FROM Report r WHERE r.targetId IN :targetIds AND r.status = :status GROUP BY r.targetId")
    List<ReportCountDto> countByTargetIdsAndStatus(
            @Param("targetIds") Collection<Long> targetIds,
            @Param("status") ReportStatus status
    );
}
