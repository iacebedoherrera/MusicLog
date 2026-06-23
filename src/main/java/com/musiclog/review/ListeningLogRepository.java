package com.musiclog.review;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeningLogRepository extends JpaRepository<ListeningLog, UUID> {

    Page<ListeningLog> findByUserIdOrderByListenedAtDesc(UUID userId, Pageable pageable);
}
