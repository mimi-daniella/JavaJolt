package com.daniella.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;
import com.daniella.repository.QuestionRepository;

@Controller
public class QuizzesController {

    private final QuestionRepository questionRepository;

    public QuizzesController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping("/quizzes")
    public String quizzes(Model model, Principal principal) {
        List<Question> allQuestions = questionRepository.findAll();
        List<String> categories = allQuestions.stream()
                .map(Question::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .sorted()
                .limit(6)
                .collect(Collectors.toList());

        // Dedicated quizzes landing page for the navbar.
        model.addAttribute("quizStats", List.of(
                Map.of("label", "Easy", "value", String.valueOf(questionRepository.findByDifficulty(Difficulty.EASY).size())),
                Map.of("label", "Medium", "value", String.valueOf(questionRepository.findByDifficulty(Difficulty.MEDIUM).size())),
                Map.of("label", "Hard", "value", String.valueOf(questionRepository.findByDifficulty(Difficulty.HARD).size())),
                Map.of("label", "Categories", "value", String.valueOf(categories.size()))
        ));

        model.addAttribute("quizLanes", List.of(
                Map.of("title", "Quick Battle", "copy", "Jump into a random Java challenge and test your instincts fast.",
                        "cta", "Start Challenge", "href", "/quiz/start"),
                Map.of("title", "Dashboard Mode", "copy", "Review progress, open settings, and track recent scores.",
                        "cta", "Open Dashboard", "href", "/dashboard"),
                Map.of("title", "Public Discovery", "copy", "Read about the platform or reach support without leaving the site.",
                        "cta", "Explore Public Pages", "href", "/public/about")
        ));
        model.addAttribute("categories", categories);
        return "user/quizzes";
    }
}
