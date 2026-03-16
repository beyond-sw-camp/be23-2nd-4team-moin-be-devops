package com.team4.moin.user.repository;

import com.team4.moin.common.domain.Category;
import com.team4.moin.user.domain.entitys.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user); // 유저별 관심 카테고리 전체 조회
}
