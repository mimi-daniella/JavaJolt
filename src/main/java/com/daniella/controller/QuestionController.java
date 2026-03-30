package com.daniella.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daniella.entity.Question;
import com.daniella.service.QuestionService;

@RestController
@RequestMapping("/questions")
public class QuestionController{
	
@Autowired
private QuestionService questionService;

@GetMapping("/easy")
public List<Question> getEasyQuestions(){
	return questionService.getEasyQuestions();
}

@GetMapping("/medium")
public List<Question> getMediumQuestions(){
	return questionService.getMediumQuestions();
}

@GetMapping("/hard")
public List<Question> getHardQuestions(){
	return questionService.getHardQuestions();
}

}
