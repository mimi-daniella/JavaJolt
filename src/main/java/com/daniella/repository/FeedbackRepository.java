package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.Feedback;
import com.daniella.enums.FeedbackStatus;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByCreatedAtDesc();
    List<Feedback> findByStatusOrderByCreatedAtDesc(FeedbackStatus status);
    long countByStatus(FeedbackStatus status);
}
