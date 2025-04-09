package com.polyu.comp3334.secure_storage_system.controller;

import com.polyu.comp3334.secure_storage_system.model.AuditLog;
import com.polyu.comp3334.secure_storage_system.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/getLogsForAdmin")
    private ResponseEntity<List<String>> getLogsForAdmin(
            @RequestParam String adminName,
            @RequestParam String adminPassword) {
        try{
            List<String> getLogs = auditLogService.getAllLogs();
            if(getLogs.isEmpty()){
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(getLogs);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

}
