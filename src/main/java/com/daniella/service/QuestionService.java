package com.daniella.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;
import com.daniella.repository.QuestionRepository;

@Service
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getEasyQuestions() {
        return questionRepository.findByDifficulty(Difficulty.EASY);
    }

    public List<Question> getMediumQuestions() {
        return questionRepository.findByDifficulty(Difficulty.MEDIUM);
    }

    public List<Question> getHardQuestions() {
        return questionRepository.findByDifficulty(Difficulty.HARD);
    }

    public List<Question> getRandomQuestions(int limit) {
        return questionRepository.findRandomQuestions(PageRequest.of(0, limit));
    }
    
    public long getTotalQuestions() {
        return questionRepository.count();
    }
}
