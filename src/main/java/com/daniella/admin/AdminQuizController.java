package com.daniella.admin;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.daniella.dto.UserResponseDTO;
import com.daniella.entity.Question;
import com.daniella.entity.QuizResult;
import com.daniella.entity.UserAnswer;
import com.daniella.repository.QuestionRepository;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserAnswerRepository;
import com.daniella.service.UserService;

@Controller
@RequestMapping("/admin/quizzes")
public class AdminQuizController {

    private final QuizResultRepository quizResultRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;

    public AdminQuizController(QuizResultRepository quizResultRepository,
                               UserAnswerRepository userAnswerRepository,
                               QuestionRepository questionRepository,
                               UserService userService) {
        this.quizResultRepository = quizResultRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.questionRepository = questionRepository;
        this.userService = userService;
    }

    @GetMapping
    public String quizzes(Model model, Principal principal) {
        addAdminContext(model, principal);

        List<QuizResult> results = quizResultRepository.findAll().stream()
                .sorted(Comparator.comparing(QuizResult::getCompletedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        List<UserAnswer> answers = userAnswerRepository.findAll();
        List<Question> questions = questionRepository.findAll();

        model.addAttribute("results", results);
        model.addAttribute("totalAttempts", results.size());
        model.addAttribute("averageScore", results.stream().mapToInt(QuizResult::getScore).average().orElse(0.0));
        model.addAttribute("topResults", results.stream()
                .sorted(Comparator.comparingInt(QuizResult::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList()));
        model.addAttribute("topPerformers", results.stream()
                .collect(Collectors.groupingBy(result -> result.getUser().getEmail(), Collectors.averagingInt(QuizResult::getScore)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList()));
        model.addAttribute("categoryPerformance", answers.stream()
                .filter(answer -> answer.getQuestion() != null && answer.getQuestion().getCategory() != null)
                .collect(Collectors.groupingBy(answer -> answer.getQuestion().getCategory(),
                        Collectors.collectingAndThen(Collectors.toList(), group -> {
                            long correct = group.stream()
                                    .filter(answer -> answer.getQuestion().getCorrectOption().equalsIgnoreCase(answer.getSelectedAnswer()))
                                    .count();
                            return Map.of(
                                    "category", group.get(0).getQuestion().getCategory(),
                                    "correctRate", group.isEmpty() ? 0.0 : (correct * 100.0) / group.size(),
                                    "attempts", group.size());
                        })))
                .values()
                .stream()
                .limit(6)
                .collect(Collectors.toList()));
        model.addAttribute("hardestQuestions", questions.stream()
                .map(question -> Map.of(
                        "text", question.getQuestionText(),
                        "category", question.getCategory() == null ? "Uncategorized" : question.getCategory(),
                        "correctRate", calculateCorrectRate(question, answers)))
                .sorted((left, right) -> Double.compare((Double) left.get("correctRate"), (Double) right.get("correctRate")))
                .limit(5)
                .collect(Collectors.toList()));
        model.addAttribute("weeklyAttempts", buildWeeklyAttempts(results));
        return "admin/quizzes";
    }

    private Map<String, Integer> buildWeeklyAttempts(List<QuizResult> results) {
        Map<String, Integer> weekly = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            int count = (int) results.stream()
                    .filter(result -> result.getCompletedAt() != null && result.getCompletedAt().toLocalDate().isEqual(day))
                    .count();
            weekly.put(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), count);
        }
        return weekly;
    }

    private double calculateCorrectRate(Question question, List<UserAnswer> answers) {
        List<UserAnswer> matching = answers.stream()
                .filter(answer -> answer.getQuestion() != null && answer.getQuestion().getId().equals(question.getId()))
                .collect(Collectors.toList());
        if (matching.isEmpty()) {
            return 0.0;
        }
        long correct = matching.stream()
                .filter(answer -> question.getCorrectOption().equalsIgnoreCase(answer.getSelectedAnswer()))
                .count();
        return (correct * 100.0) / matching.size();
    }

    private void addAdminContext(Model model, Principal principal) {
        UserResponseDTO currentAdmin = userService.getByEmail(principal.getName());
        model.addAttribute("activePage", "quizzes");
        model.addAttribute("firstName", currentAdmin.firstName());
        model.addAttribute("lastName", currentAdmin.lastName());
        model.addAttribute("email", currentAdmin.email());
    }
}
