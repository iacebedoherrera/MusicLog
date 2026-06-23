package com.musiclog.user;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    long deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    List<Follow> findByFollowedIdOrderByCreatedAtDesc(UUID followedId);

    List<Follow> findByFollowerIdOrderByCreatedAtDesc(UUID followerId);
}
