package com.musiclog.social;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<ActivityEntry, UUID> {

    Page<ActivityEntry> findByActorIdOrderByCreatedAtDesc(UUID actorId, Pageable pageable);

    List<ActivityEntry> findTop20ByActorIdOrderByCreatedAtDesc(UUID actorId);
}
