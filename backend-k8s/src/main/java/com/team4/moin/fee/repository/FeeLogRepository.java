package com.team4.moin.fee.repository;

import com.team4.moin.fee.domain.FeeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeLogRepository extends JpaRepository<FeeLog,Long> {
}
