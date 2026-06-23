package com.musiclog.review;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByUserIdAndTargetMbid(UUID userId, String targetMbid);

    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Review> findByTargetMbidAndTargetTypeOrderByCreatedAtDesc(String targetMbid, ReviewTargetType targetType, Pageable pageable);
}
