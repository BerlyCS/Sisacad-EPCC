package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.AuditLog;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);
}
