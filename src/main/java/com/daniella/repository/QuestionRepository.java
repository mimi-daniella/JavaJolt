package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;
import com.daniella.enums.QuestionStatus;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
	
	List<Question> findByDifficulty(Difficulty difficulty);
	List<Question> findByStatus(QuestionStatus status);

	// Quiz frontend support: load a mixed random set for the real /quiz/start route.
	@Query(value = "SELECT * FROM questions WHERE status = 'ACTIVE' ORDER BY RAND() LIMIT :limit", nativeQuery = true)
	List<Question> findRandomQuestions(@Param("limit") int limit);

}