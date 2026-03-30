package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.User;
import com.daniella.entity.UserAnswer;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
	
	List<UserAnswer> findByUser(User user);   // Example: get all answers by a user



}
