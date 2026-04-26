package com.daniella.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.QuizResult;
import com.daniella.entity.User;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
	
	 List<QuizResult> findByUser(User user);
	 List<QuizResult> findTop10ByOrderByCompletedAtDesc();
	 List<QuizResult> findByUserAndCompletedAtBetween(User user, LocalDateTime start, LocalDateTime end);
	 List<QuizResult> findTop10ByOrderByPercentageScoreDescCompletedAtAsc();
     void deleteByUser(User user);

	
}
