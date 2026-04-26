package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daniella.entity.Feedback;
import com.daniella.enums.FeedbackStatus;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    java.util.List<Feedback> findAllByOrderByCreatedAtDesc();
    java.util.List<Feedback> findByStatusOrderByCreatedAtDesc(FeedbackStatus status);
    long countByStatus(FeedbackStatus status);
}
