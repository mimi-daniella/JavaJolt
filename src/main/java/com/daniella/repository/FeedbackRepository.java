package com.daniella.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}