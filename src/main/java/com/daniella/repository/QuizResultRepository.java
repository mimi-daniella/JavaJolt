package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.QuizResult;
import com.daniella.entity.User;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
	
	 List<QuizResult> findByUser(User user);

	
}
