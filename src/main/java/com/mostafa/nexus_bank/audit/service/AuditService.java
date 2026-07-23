package com.mostafa.nexus_bank.audit.service;

import com.mostafa.nexus_bank.audit.dto.response.AuditPageResponse;
import com.mostafa.nexus_bank.audit.entity.Audit;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditService {

    void saveAudit(Audit audit);

    AuditPageResponse getAllAuditLogs(Pageable pageable);

    AuditPageResponse getAuditLogsByUserId(UUID userId, Pageable pageable);

    AuditPageResponse getAuditLogsByEventType(String eventType, Pageable pageable);
}
