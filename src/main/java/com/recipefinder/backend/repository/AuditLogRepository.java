package com.recipefinder.backend.repository;

import com.recipefinder.backend.domain.AuditLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends CrudRepository<AuditLog, Long> {
    List<AuditLog> findAll();
}
