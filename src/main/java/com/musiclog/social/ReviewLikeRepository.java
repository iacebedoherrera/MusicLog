package com.musiclog.social;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);

    long deleteByUserIdAndReviewId(UUID userId, UUID reviewId);

    long countByReviewId(UUID reviewId);
}
