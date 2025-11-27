package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.AuditLog;
import com.application.sisacadepcc.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(Long userId, String action, String resourceId, String details, String ipAddress) {
        AuditLog log = new AuditLog(userId, action, resourceId, details, ipAddress);
        auditLogRepository.save(log);
    }
}
