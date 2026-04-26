package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.AdminActionLog;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    List<AdminActionLog> findTop10ByOrderByCreatedAtDesc();
    List<AdminActionLog> findTop20ByOrderByCreatedAtDesc();
}
