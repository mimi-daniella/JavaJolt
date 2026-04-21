package com.daniella.controller;

import com.daniella.entity.Question;
import com.daniella.entity.QuizResult;
import com.daniella.entity.User;
import com.daniella.repository.QuestionRepository;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    public QuizController(QuestionRepository questionRepository,
                          QuizResultRepository quizResultRepository,
                          UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.quizResultRepository = quizResultRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/start")
    public String startQuiz(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
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
                    if (q.getCorrectOption().equals(submittedAnswer)) {
                        score++;
                    }
                }
            }
        }

        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                QuizResult result = new QuizResult();
                result.setUser(user);
                result.setScore(score);
                result.setCompletedAt(LocalDateTime.now());
                quizResultRepository.save(result);
            }
        }

        model.addAttribute("score", score);
        model.addAttribute("total", total);
        return "user/result";
    }
}
