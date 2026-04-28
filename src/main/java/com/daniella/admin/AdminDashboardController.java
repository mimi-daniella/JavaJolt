package com.daniella.admin;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.daniella.dto.UserResponseDTO;
import com.daniella.entity.AdminActionLog;
import com.daniella.entity.Feedback;
import com.daniella.entity.Question;
import com.daniella.entity.QuizResult;
import com.daniella.entity.User;
import com.daniella.enums.QuestionStatus;
import com.daniella.enums.UserStatus;
import com.daniella.repository.AdminActionLogRepository;
import com.daniella.repository.FeedbackRepository;
import com.daniella.repository.QuestionRepository;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserRepository;
import com.daniella.service.SystemSettingService;
import com.daniella.service.UserService;

@Controller
public class AdminDashboardController {

	private final UserRepository userRepository;
	private final UserService userService;
	private final QuestionRepository questionRepository;
	private final QuizResultRepository quizResultRepository;
	private final FeedbackRepository feedbackRepository;
	private final AdminActionLogRepository adminActionLogRepository;
	private final SystemSettingService systemSettingService;

	public AdminDashboardController(UserRepository userRepository, UserService userService,
			QuestionRepository questionRepository, QuizResultRepository quizResultRepository,
			FeedbackRepository feedbackRepository, AdminActionLogRepository adminActionLogRepository,
			SystemSettingService systemSettingService) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.questionRepository = questionRepository;
		this.quizResultRepository = quizResultRepository;
		this.feedbackRepository = feedbackRepository;
		this.adminActionLogRepository = adminActionLogRepository;
		this.systemSettingService = systemSettingService;
	}

	@GetMapping("/admin/dashboard")
	public String dashboard(Model model, Principal principal) {
		addAdminContext(model, principal);

		List<User> users = userRepository.findAll();
		List<Question> questions = questionRepository.findAll();
		List<QuizResult> results = quizResultRepository.findAll();
		List<Feedback> feedbackItems = feedbackRepository.findAllByOrderByCreatedAtDesc();

		model.addAttribute("totalUsers", users.size());
		model.addAttribute("activeUsers", users.stream().filter(user -> user.getStatus() == UserStatus.ACTIVE).count());
		model.addAttribute("suspendedUsers",
				users.stream().filter(user -> user.getStatus() == UserStatus.SUSPENDED).count());
		model.addAttribute("verifiedUsers", users.stream().filter(User::isVerified).count());
		model.addAttribute("totalQuestions", questions.size());
		model.addAttribute("activeQuestions",
				questions.stream().filter(question -> question.getStatus() == QuestionStatus.ACTIVE).count());
		model.addAttribute("draftQuestions",
				questions.stream().filter(question -> question.getStatus() == QuestionStatus.DRAFT).count());
		model.addAttribute("totalQuizzes", results.size());
		model.addAttribute("averageScore", results.stream().mapToInt(QuizResult::getScore).average().orElse(0.0));
		model.addAttribute("completionRate", users.isEmpty() ? 0.0
				: (results.stream().map(result -> result.getUser().getId()).distinct().count() * 100.0) / users.size());
		model.addAttribute("mostUsedCategory", findMostUsedCategory(questions));
		model.addAttribute("maintenanceBanner", systemSettingService.get(SystemSettingService.MAINTENANCE_BANNER, ""));
		model.addAttribute("announcement", systemSettingService.get(SystemSettingService.ANNOUNCEMENT, ""));
		model.addAttribute("alerts", buildAlerts(users, questions, feedbackItems));
		model.addAttribute("recentActivity", buildRecentActivity(results, feedbackItems,
				adminActionLogRepository.findTop10ByOrderByCreatedAtDesc()));

		return "admin/dashboard";
	}

	private void addAdminContext(Model model, Principal principal) {
		UserResponseDTO currentAdmin = userService.getByEmail(principal.getName());
		model.addAttribute("activePage", "dashboard");
		model.addAttribute("firstName", currentAdmin.firstName());
		model.addAttribute("lastName", currentAdmin.lastName());
		model.addAttribute("email", currentAdmin.email());
	}

	private String findMostUsedCategory(List<Question> questions) {
		return questions.stream()
				.filter(question -> question.getCategory() != null && !question.getCategory().isBlank())
				.collect(Collectors.groupingBy(Question::getCategory, Collectors.counting())).entrySet().stream()
				.max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
	}

	private List<Map<String, String>> buildAlerts(List<User> users, List<Question> questions,
			List<Feedback> feedbackItems) {
		List<Map<String, String>> alerts = new ArrayList<>();

		if (questions.size() < 100) {
			alerts.add(alert("Question bank below project minimum requirement.", "warning"));
		}
		long unverifiedUsers = users.stream().filter(user -> !user.isVerified()).count();
		if (unverifiedUsers > 0) {
			alerts.add(alert(unverifiedUsers + " account(s) are still unverified.", "info"));
		}
		long suspendedUsers = users.stream().filter(user -> user.getStatus() == UserStatus.SUSPENDED).count();
		if (suspendedUsers > 0) {
			alerts.add(alert(suspendedUsers + " user account(s) are currently suspended.", "warning"));
		}
		String maintenanceBanner = systemSettingService.get(SystemSettingService.MAINTENANCE_BANNER, "");
		if (!maintenanceBanner.isBlank()) {
			alerts.add(alert("Maintenance banner is active on the platform.", "danger"));
		}
		if (alerts.isEmpty()) {
			alerts.add(alert("System looks healthy. No urgent admin action required right now.", "success"));
		}
		return alerts;
	}

	private Map<String, String> alert(String text, String tone) {
		Map<String, String> item = new LinkedHashMap<>();
		item.put("text", text);
		item.put("tone", tone);
		return item;
	}

	private List<Map<String, String>> buildRecentActivity(List<QuizResult> results, List<Feedback> feedbackItems,
			List<AdminActionLog> adminLogs) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
		List<Map<String, String>> activities = new ArrayList<>();

		results.stream()
				.sorted(Comparator
						.comparing(QuizResult::getCompletedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(4)
				.forEach(result -> activities.add(activity("Quiz submitted",
						result.getUser().getEmail() + " scored " + result.getScore(),
						result.getCompletedAt() != null ? result.getCompletedAt().format(formatter) : "Recently")));

		feedbackItems.stream().limit(3)
				.forEach(feedback -> activities.add(activity("Feedback received", feedback.getEmail(),
						feedback.getCreatedAt() != null ? feedback.getCreatedAt().format(formatter) : "Recently")));

		adminLogs.stream().limit(3).forEach(
				log -> activities.add(activity("Admin action", log.getActorEmail() + " - " + log.getActionType(),
						log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "Recently")));

		return activities.stream().limit(8).collect(Collectors.toList());
	}

	private Map<String, String> activity(String title, String description, String time) {
		Map<String, String> item = new LinkedHashMap<>();
		item.put("title", title);
		item.put("description", description);
		item.put("time", time);
		return item;
	}
}