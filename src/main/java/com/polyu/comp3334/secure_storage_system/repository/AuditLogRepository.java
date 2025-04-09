package com.polyu.comp3334.secure_storage_system.repository;

import com.polyu.comp3334.secure_storage_system.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    AuditLog findByUsername(String username);
}