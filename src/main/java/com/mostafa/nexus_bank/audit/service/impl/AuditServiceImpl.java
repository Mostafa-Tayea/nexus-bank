package com.mostafa.nexus_bank.audit.service.impl;

import com.mostafa.nexus_bank.audit.dto.response.AuditPageResponse;
import com.mostafa.nexus_bank.audit.dto.response.AuditResponse;
import com.mostafa.nexus_bank.audit.entity.Audit;
import com.mostafa.nexus_bank.audit.mapper.AuditMapper;
import com.mostafa.nexus_bank.audit.repository.AuditRepository;
import com.mostafa.nexus_bank.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;

    @Override
    @Transactional
    public void saveAudit(Audit audit) {
        auditRepository.save(audit);
        log.debug("Audit saved: {} - {}", audit.getEventType(), audit.getAction());
    }

    @Override
    @Transactional(readOnly = true)
    public AuditPageResponse getAllAuditLogs(Pageable pageable) {
        Page<Audit> page = auditRepository.findAllByOrderByTimestampDesc(pageable);

        List<AuditResponse> content = page.getContent().stream()
                .map(auditMapper::toResponse)
                .toList();

        return AuditPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditPageResponse getAuditLogsByUserId(UUID userId, Pageable pageable) {
        Page<Audit> page = auditRepository.findByUserIdOrderByTimestampDesc(userId, pageable);

        List<AuditResponse> content = page.getContent().stream()
                .map(auditMapper::toResponse)
                .toList();

        return AuditPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditPageResponse getAuditLogsByEventType(String eventType, Pageable pageable) {
        Page<Audit> page = auditRepository.findByEventTypeOrderByTimestampDesc(eventType, pageable);

        List<AuditResponse> content = page.getContent().stream()
                .map(auditMapper::toResponse)
                .toList();

        return AuditPageResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
