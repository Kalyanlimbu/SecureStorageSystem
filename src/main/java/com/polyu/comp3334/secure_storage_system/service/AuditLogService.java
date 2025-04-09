package com.polyu.comp3334.secure_storage_system.service;

import java.security.PublicKey;
import java.security.Signature;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.polyu.comp3334.secure_storage_system.model.AuditLog;
import com.polyu.comp3334.secure_storage_system.repository.AuditLogRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void logInLog(String username){
        String details = username + " logged in";
        AuditLog log = new AuditLog(LocalDateTime.now(), username, "LOGIN", details);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logOutLog(String username){
        String details = username + " logged out";
        AuditLog log = new AuditLog(LocalDateTime.now(), username, "LOGOUT", details);
        auditLogRepository.save(log);
    }

    @Transactional
    public void uploadLog(String username, String filename){
        String details = filename + " is uploaded";
        AuditLog log = new AuditLog(LocalDateTime.now(), username, "UPLOAD", details);
        auditLogRepository.save(log);
    }

    @Transactional
    public void deleteLog(String username, String filename){
        String details = filename + " is deleted";
        AuditLog log = new AuditLog(LocalDateTime.now(), username, "DELETE", details);
        auditLogRepository.save(log);
    }

    @Transactional
    public void shareLog(String username, String filename, String designatedUserName){
        String details = filename + " is shared to " + designatedUserName;
        AuditLog log = new AuditLog(LocalDateTime.now(), username, "SHARE", details);
        auditLogRepository.save(log);
    }

    @Transactional
    public List<String> getAllLogs(){
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        List<String> allLogs = new ArrayList<>();
        for(AuditLog log: auditLogs){
            allLogs.add("Timestamp: " + log.getTimestamp() + ", Username: " + log.getUsername() + ", Action: " + log.getAction() + ", Details: " + log.getDetails());
        }
        return allLogs;
    }
}