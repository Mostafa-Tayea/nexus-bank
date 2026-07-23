package com.mostafa.nexus_bank.audit.repository;

import com.mostafa.nexus_bank.audit.entity.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditRepository extends JpaRepository<Audit, UUID> {

    Page<Audit> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<Audit> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    Page<Audit> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Audit a WHERE a.timestamp < :cutoff")
    void deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);
}
