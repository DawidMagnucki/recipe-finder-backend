package com.recipefinder.backend.service;

import com.recipefinder.backend.domain.AuditLog;
import com.recipefinder.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void log(String action, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }
}
