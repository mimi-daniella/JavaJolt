package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
	
	List<Question> findByDifficulty(Difficulty difficulty);

}
