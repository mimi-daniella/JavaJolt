package com.daniella.service;

import java.security.Principal;

import org.springframework.stereotype.Service;

import com.daniella.entity.AdminActionLog;
import com.daniella.repository.AdminActionLogRepository;

@Service
public class AdminAuditService {

    private final AdminActionLogRepository adminActionLogRepository;

    public AdminAuditService(AdminActionLogRepository adminActionLogRepository) {
        this.adminActionLogRepository = adminActionLogRepository;
    }

    public void log(Principal principal, String actionType, String targetType, String targetId, String details) {
        AdminActionLog log = new AdminActionLog();
        log.setActorEmail(principal != null ? principal.getName() : "system");
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        adminActionLogRepository.save(log);
    }
}
