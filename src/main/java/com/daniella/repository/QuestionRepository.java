package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
	
	List<Question> findByDifficulty(Difficulty difficulty);

	// Quiz frontend support: load a mixed random set for the real /quiz/start route.
	@Query(value = "SELECT * FROM questions ORDER BY RAND() LIMIT 10", nativeQuery = true)
	List<Question> findRandomQuestions();

}
