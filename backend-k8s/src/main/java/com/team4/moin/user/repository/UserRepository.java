package com.team4.moin.user.repository;

import com.team4.moin.user.domain.entitys.User;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findAllByEmail(String email);
    @Query("select u from User u left join fetch u.address where u.email = :email")
    Optional<User> findAllByEmailWithAddress(@Param("email") String email);

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByEmailAndDelYn(String email, String delYn);

    @Query(value = "select u from User u left join fetch u.address",
            countQuery = "select count(u) from User u")
    Page<User> findAllWithAddress(Pageable pageable);

    // 다른 트랜잭션은 이 유저 정보를 읽으려 해도 내 작업이 끝날 때까지 대기 , 비관적 락으로 동시성 이슈 해결
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);
    // 1. 단순 ID만 필요할 때 (가장 가벼움, 커넥션 점유 최소화)
    @Query("SELECT u.id FROM User u WHERE u.email = :email AND u.delYn = 'No'")
    Optional<Long> findIdByEmail(@Param("email") String email);

    // 2. 유저 정보와 연관 데이터(Address, Categories)가 모두 필요할 때 (N+1 방지)
    @EntityGraph(attributePaths = {"address", "categories"})
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.delYn = :delYn")
    Optional<User> findByEmailWithDetails(@Param("email") String email, @Param("delYn") String delYn);
}
