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
import com.daniella.enums.QuestionStatus;
import com.daniella.service.GamificationService;
import com.daniella.service.SystemSettingService;
import com.daniella.repository.QuestionRepository;

@Controller
public class QuizzesController {

    private final QuestionRepository questionRepository;
    private final SystemSettingService systemSettingService;
    private final GamificationService gamificationService;

    public QuizzesController(QuestionRepository questionRepository,
                             SystemSettingService systemSettingService,
                             GamificationService gamificationService) {
        this.questionRepository = questionRepository;
        this.systemSettingService = systemSettingService;
        this.gamificationService = gamificationService;
    }

    @GetMapping("/quizzes")
    public String quizzes(Model model, Principal principal) {
        List<Question> allQuestions = questionRepository.findByStatus(QuestionStatus.ACTIVE);
        List<String> categories = allQuestions.stream()
                .map(Question::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        Map<String, Integer> quizStats = Map.of(
                "Easy", questionRepository.findByDifficulty(Difficulty.EASY).size(),
                "Medium", questionRepository.findByDifficulty(Difficulty.MEDIUM).size(),
                "Hard", questionRepository.findByDifficulty(Difficulty.HARD).size(),
                "Categories", categories.size());

        model.addAttribute("quizStats", quizStats);
        model.addAttribute("categories", categories);
        model.addAttribute("dailyChallengeEnabled", systemSettingService.getBoolean(SystemSettingService.DAILY_CHALLENGE_ENABLED, true));
        model.addAttribute("dailyChallengeCategory", systemSettingService.get(SystemSettingService.DAILY_CHALLENGE_CATEGORY, "Java"));
        model.addAttribute("featuredChallengeTitle", systemSettingService.get(SystemSettingService.FEATURED_CHALLENGE_TITLE, "Barista Boss Battle"));
        model.addAttribute("defaultCategory", systemSettingService.get(SystemSettingService.DEFAULT_CATEGORY, ""));
        model.addAttribute("defaultDifficulty", systemSettingService.get(SystemSettingService.DEFAULT_DIFFICULTY, ""));
        model.addAttribute("defaultTimerMinutes", 10);
        model.addAttribute("leaderboard", gamificationService.leaderboardSlice());
        return "user/quizzes";
    }
}