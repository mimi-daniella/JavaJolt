package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	
	List<Question> findByDifficulty(Difficulty difficulty);

}
