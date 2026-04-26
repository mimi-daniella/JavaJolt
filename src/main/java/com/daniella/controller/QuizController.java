package com.daniella.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.daniella.entity.Question;
import com.daniella.entity.QuizResult;
import com.daniella.entity.User;
import com.daniella.entity.UserAnswer;
import com.daniella.enums.QuestionStatus;
import com.daniella.repository.QuestionRepository;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserAnswerRepository;
import com.daniella.repository.UserRepository;
import com.daniella.service.GamificationService;
import com.daniella.service.SystemSettingService;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private static final String ACTIVE_QUIZ_IDS = "ACTIVE_QUIZ_IDS";
    private static final String ACTIVE_QUIZ_TOKEN = "ACTIVE_QUIZ_TOKEN";

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final SystemSettingService systemSettingService;
    private final GamificationService gamificationService;

    public QuizController(QuestionRepository questionRepository,
                          QuizResultRepository quizResultRepository,
                          UserRepository userRepository,
                          UserAnswerRepository userAnswerRepository,
                          SystemSettingService systemSettingService,
                          GamificationService gamificationService) {
        this.questionRepository = questionRepository;
        this.quizResultRepository = quizResultRepository;
        this.userRepository = userRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.systemSettingService = systemSettingService;
        this.gamificationService = gamificationService;
    }

    @GetMapping("/start")
    public String startQuiz(@RequestParam(required = false) String category,
                            @RequestParam(required = false) String difficulty,
                            @RequestParam(required = false, defaultValue = "false") boolean dailyChallenge,
                            @RequestParam(required = false, defaultValue = "false") boolean timerEnabled,
                            @RequestParam(required = false, defaultValue = "10") int timerMinutes,
                            HttpSession session,
                            Model model,
                            Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (!systemSettingService.getBoolean(SystemSettingService.ALLOW_RETAKES, true)) {
            LocalDate today = LocalDate.now();
            boolean hasPlayedToday = !quizResultRepository.findByUserAndCompletedAtBetween(
                    user, today.atStartOfDay(), today.plusDays(1).atStartOfDay()).isEmpty();
            if (hasPlayedToday) {
                model.addAttribute("error", "Retakes are currently disabled. Come back tomorrow for a fresh challenge.");
                model.addAttribute("score", 0);
                model.addAttribute("total", 0);
                model.addAttribute("passMark", systemSettingService.getInt(SystemSettingService.PASS_MARK, 60));
                model.addAttribute("passed", false);
                clearActiveQuiz(session);
                return "user/result";
            }
        }

        int questionLimit = systemSettingService.getInt(SystemSettingService.QUESTIONS_PER_ROUND, 10);
        String effectiveCategory = dailyChallenge
                ? systemSettingService.get(SystemSettingService.DAILY_CHALLENGE_CATEGORY, "")
                : normalizeFilter(category, systemSettingService.get(SystemSettingService.DEFAULT_CATEGORY, ""));
        String effectiveDifficulty = normalizeFilter(difficulty, systemSettingService.get(SystemSettingService.DEFAULT_DIFFICULTY, ""));
        int safeTimerMinutes = Math.max(1, Math.min(timerMinutes, 30));

        List<Question> questions = questionRepository.findByStatus(QuestionStatus.ACTIVE);
        if (!effectiveCategory.isBlank()) {
            questions = questions.stream()
                    .filter(question -> question.getCategory() != null && question.getCategory().equalsIgnoreCase(effectiveCategory))
                    .collect(Collectors.toList());
        }
        if (!effectiveDifficulty.isBlank()) {
            questions = questions.stream()
                    .filter(question -> question.getDifficulty() != null && question.getDifficulty().name().equalsIgnoreCase(effectiveDifficulty))
                    .collect(Collectors.toList());
        }
        Collections.shuffle(questions);
        questions = questions.stream().limit(questionLimit).collect(Collectors.toList());

        String quizToken = UUID.randomUUID().toString();
        session.setAttribute(ACTIVE_QUIZ_TOKEN, quizToken);
        session.setAttribute(ACTIVE_QUIZ_IDS, questions.stream().map(Question::getId).collect(Collectors.toList()));

        model.addAttribute("questions", questions);
        model.addAttribute("quizToken", quizToken);
        model.addAttribute("timerEnabled", timerEnabled);
        model.addAttribute("timerMinutes", safeTimerMinutes);
        model.addAttribute("selectedCategory", effectiveCategory);
        model.addAttribute("selectedDifficulty", effectiveDifficulty);
        model.addAttribute("dailyChallenge", dailyChallenge);
        model.addAttribute("featuredTitle", systemSettingService.get(SystemSettingService.FEATURED_CHALLENGE_TITLE, "JavaJolt Challenge"));
        return "user/take-quiz";
    }

    @PostMapping("/submit")
    public String submitQuiz(@RequestParam Map<String, String> allParams,
                             @RequestParam(required = false) String selectedCategory,
                             @RequestParam(required = false) String selectedDifficulty,
                             @RequestParam(required = false) String quizToken,
                             HttpSession session,
                             Model model,
                             Principal principal) {
        @SuppressWarnings("unchecked")
        List<Long> expectedQuestionIds = (List<Long>) session.getAttribute(ACTIVE_QUIZ_IDS);
        String expectedToken = (String) session.getAttribute(ACTIVE_QUIZ_TOKEN);

        if (expectedQuestionIds == null || expectedQuestionIds.isEmpty() || expectedToken == null || !expectedToken.equals(quizToken)) {
            buildInvalidQuizResponse(model, "Your quiz session is no longer valid. Please start a new quiz.");
            clearActiveQuiz(session);
            return "user/result";
        }

        int score = 0;
        int total = 0;
        List<Long> answeredQuestionIds = new ArrayList<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("question_")) {
                Long questionId = Long.parseLong(entry.getKey().replace("question_", ""));
                if (!expectedQuestionIds.contains(questionId)) {
                    buildInvalidQuizResponse(model, "Submitted quiz data did not match the active quiz.");
                    clearActiveQuiz(session);
                    return "user/result";
                }

                String submittedAnswer = entry.getValue();
                answeredQuestionIds.add(questionId);

                Question q = questionRepository.findById(questionId).orElse(null);
                if (q != null) {
                    total++;
                    if (q.getCorrectOption().equals(submittedAnswer)) {
                        score++;
                    }
                }
            }
        }

        if (answeredQuestionIds.size() != expectedQuestionIds.size()) {
            buildInvalidQuizResponse(model, "Please answer every question before submitting.");
            model.addAttribute("total", expectedQuestionIds.size());
            clearActiveQuiz(session);
            return "user/result";
        }

        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                if (!systemSettingService.getBoolean(SystemSettingService.ALLOW_RETAKES, true)) {
                    LocalDate today = LocalDate.now();
                    boolean hasPlayedToday = !quizResultRepository.findByUserAndCompletedAtBetween(
                            user, today.atStartOfDay(), today.plusDays(1).atStartOfDay()).isEmpty();
                    if (hasPlayedToday) {
                        buildInvalidQuizResponse(model, "Retakes are currently disabled. Come back tomorrow for a fresh challenge.");
                        clearActiveQuiz(session);
                        return "user/result";
                    }
                }

                double percentage = total == 0 ? 0.0 : (score * 100.0) / total;
                boolean passed = percentage >= systemSettingService.getInt(SystemSettingService.PASS_MARK, 60);
                QuizResult result = new QuizResult();
                result.setUser(user);
                result.setScore(score);
                result.setTotalQuestions(total);
                result.setPercentageScore(percentage);
                result.setPassed(passed);
                result.setCategoryFocus(selectedCategory);
                result.setDifficultyFocus(selectedDifficulty);
                result.setCompletedAt(LocalDateTime.now());
                int xpEarned = gamificationService.rewardUser(user, score, total, LocalDate.now());
                result.setXpEarned(xpEarned);
                quizResultRepository.save(result);

                for (Map.Entry<String, String> entry : allParams.entrySet()) {
                    if (entry.getKey().startsWith("question_")) {
                        Long questionId = Long.parseLong(entry.getKey().replace("question_", ""));
                        Question question = questionRepository.findById(questionId).orElse(null);
                        if (question != null) {
                            UserAnswer answer = new UserAnswer();
                            answer.setUser(user);
                            answer.setQuestion(question);
                            answer.setSelectedAnswer(entry.getValue());
                            answer.setAnsweredAt(LocalDateTime.now());
                            userAnswerRepository.save(answer);
                        }
                    }
                }
            }
        }

        model.addAttribute("score", score);
        model.addAttribute("total", total);
        model.addAttribute("passMark", systemSettingService.getInt(SystemSettingService.PASS_MARK, 60));
        model.addAttribute("passed", total > 0 && ((score * 100.0) / total) >= systemSettingService.getInt(SystemSettingService.PASS_MARK, 60));
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("xp", user.getXp());
                model.addAttribute("rankTitle", user.getRankTitle());
                model.addAttribute("streakCount", user.getStreakCount());
            });
        }
        clearActiveQuiz(session);
        return "user/result";
    }

    private String normalizeFilter(String requested, String fallback) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }

    private void buildInvalidQuizResponse(Model model, String error) {
        model.addAttribute("error", error);
        model.addAttribute("score", 0);
        model.addAttribute("total", 0);
        model.addAttribute("passMark", systemSettingService.getInt(SystemSettingService.PASS_MARK, 60));
        model.addAttribute("passed", false);
    }

    private void clearActiveQuiz(HttpSession session) {
        session.removeAttribute(ACTIVE_QUIZ_IDS);
        session.removeAttribute(ACTIVE_QUIZ_TOKEN);
    }
}
