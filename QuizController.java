package com.daniella.controller;

import com.daniella.entity.Question;
import com.daniella.entity.QuizResult;
import com.daniella.repository.QuestionRepository;
import com.daniella.repository.QuizResultRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;

    public QuizController(QuestionRepository questionRepository, QuizResultRepository quizResultRepository) {
        this.questionRepository = questionRepository;
        this.quizResultRepository = quizResultRepository;
    }

    @GetMapping("/start")
    public String startQuiz(Model model) {
        List<Question> questions = questionRepository.findRandomQuestions();
        model.addAttribute("questions", questions);
        return "user/take-quiz";
    }

    @PostMapping("/submit")
    public String submitQuiz(@RequestParam Map<String, String> allParams, Model model, Principal principal) {
        int score = 0;
        int total = 0;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("question_")) {
                Long questionId = Long.parseLong(entry.getKey().replace("question_", ""));
                String submittedAnswer = entry.getValue();
                
                Question q = questionRepository.findById(questionId).orElse(null);
                if (q != null) {
                    total++;
                    if (q.getCorrectAnswer().equals(submittedAnswer)) {
                        score++;
                    }
                }
            }
        }

        if (principal != null) {
            QuizResult result = new QuizResult(principal.getName(), score, total);
            quizResultRepository.save(result);
        }

        model.addAttribute("score", score);
        model.addAttribute("total", total);
        return "user/result";
    }
}