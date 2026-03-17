package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.AuditLog;
import com.recipefinder.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    @GetMapping("/count")
    public long getLogsCount() {
        return auditLogRepository.count();
    }

    @DeleteMapping("/clear")
    public void clearLogs() {
        auditLogRepository.deleteAll();
    }

    @GetMapping("/filter")
    public List<AuditLog> getLogsByAction(@RequestParam String action) {
        return auditLogRepository.findAll().stream()
                .filter(log -> log.getAction().equalsIgnoreCase(action))
                .toList();
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Audit System is online. Total logs: " + auditLogRepository.count();
    }
}
