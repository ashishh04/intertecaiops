package com.juviai.user.repo;

import com.juviai.user.domain.CoinCategory;
import com.juviai.user.domain.UserCoinCategoryProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCoinCategoryProgressRepository extends JpaRepository<UserCoinCategoryProgress, UUID> {
    Optional<UserCoinCategoryProgress> findByUserIdAndCategory(UUID userId, CoinCategory category);
}
