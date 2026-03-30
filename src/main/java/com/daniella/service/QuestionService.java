package com.daniella.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;
import com.daniella.repository.QuestionRepository;

@Service
public class QuestionService {

@Autowired
private QuestionRepository questionRepository;

public List<Question> getEasyQuestions(){
	return questionRepository.findByDifficulty(Difficulty.EASY);
}

	
public List<Question> getMediumQuestions(){
	return questionRepository.findByDifficulty(Difficulty.MEDIUM);
}


public List<Question> getHardQuestions(){
	return questionRepository.findByDifficulty(Difficulty.HARD);
}

}
