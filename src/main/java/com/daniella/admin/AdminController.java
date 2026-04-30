package com.daniella.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.daniella.dto.UserResponseDTO;
import com.daniella.entity.Feedback;
import com.daniella.entity.User;
import com.daniella.repository.AdminActionLogRepository;
import com.daniella.repository.FeedbackRepository;
import com.daniella.repository.QuestionRepository;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserRepository;
import com.daniella.service.AdminAuditService;
import com.daniella.service.SystemSettingService;
import com.daniella.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final UserService userService;
	private final UserRepository userRepository;
	private final FeedbackRepository feedbackRepository;
	private final QuestionRepository questionRepository;
	private final QuizResultRepository quizResultRepository;
	private final AdminActionLogRepository adminActionLogRepository;
	private final SystemSettingService systemSettingService;
	private final AdminAuditService adminAuditService;
	private final ObjectMapper objectMapper;

	public AdminController(UserService userService, UserRepository userRepository,
			FeedbackRepository feedbackRepository, QuestionRepository questionRepository,
			QuizResultRepository quizResultRepository, AdminActionLogRepository adminActionLogRepository,
			SystemSettingService systemSettingService, AdminAuditService adminAuditService, ObjectMapper objectMapper) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.feedbackRepository = feedbackRepository;
		this.questionRepository = questionRepository;
		this.quizResultRepository = quizResultRepository;
		this.adminActionLogRepository = adminActionLogRepository;
		this.systemSettingService = systemSettingService;
		this.adminAuditService = adminAuditService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {
		addAdminContext(model, principal, "profile");
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("role", user.getRole() != null ? user.getRole().name() : "N/A");
		model.addAttribute("status", user.getStatus() != null ? user.getStatus().name() : "N/A");
		model.addAttribute("createdAt", user.getCreatedAt());
		model.addAttribute("lastLoginAt", user.getLastLoginAt());
		return "admin/profile";
	}

	@GetMapping("/messaging")
	public String messaging(@RequestParam(required = false) String status, Model model, Principal principal) {
		addAdminContext(model, principal, "messaging");
		List<Feedback> feedback = feedbackRepository.findAllByOrderByCreatedAtDesc();
		model.addAttribute("messages", feedback);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("announcement", systemSettingService.get(SystemSettingService.ANNOUNCEMENT, ""));
		model.addAttribute("maintenanceBanner", systemSettingService.get(SystemSettingService.MAINTENANCE_BANNER, ""));
		return "/admin/messaging";
	}

	public String updateMessageStatus(@PathVariable Long id, Principal principal,
			RedirectAttributes redirectAttributes) {
		Feedback feedback = feedbackRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Feedback not found"));
		redirectAttributes.addFlashAttribute("success", "Message status updated.");
		return "redirect:/admin/messaging";
	}

	@PostMapping("/settings")
	public String settingsSave(@RequestParam Map<String, String> params, Principal principal,
			RedirectAttributes redirectAttributes) {
		systemSettingService.save(SystemSettingService.QUESTIONS_PER_ROUND,
				params.getOrDefault("questionsPerRound", "10"), "quiz");
		systemSettingService.save(SystemSettingService.TIMER_ENABLED,
				String.valueOf(params.containsKey("timerEnabled")), "quiz");
		systemSettingService.save(SystemSettingService.PASS_MARK, params.getOrDefault("passMark", "60"), "quiz");
		systemSettingService.save(SystemSettingService.ALLOW_RETAKES,
				String.valueOf(params.containsKey("allowRetakes")), "quiz");
		systemSettingService.save(SystemSettingService.DEFAULT_CATEGORY, params.getOrDefault("defaultCategory", ""),
				"quiz");
		systemSettingService.save(SystemSettingService.DEFAULT_DIFFICULTY, params.getOrDefault("defaultDifficulty", ""),
				"quiz");
		systemSettingService.save(SystemSettingService.DAILY_CHALLENGE_ENABLED,
				String.valueOf(params.containsKey("dailyChallengeEnabled")), "game");
		systemSettingService.save(SystemSettingService.FEATURED_CHALLENGE_TITLE,
				params.getOrDefault("featuredChallengeTitle", ""), "game");
		systemSettingService.save(SystemSettingService.DAILY_CHALLENGE_CATEGORY,
				params.getOrDefault("dailyChallengeCategory", ""), "game");
		systemSettingService.save(SystemSettingService.BASE_XP_PER_CORRECT,
				params.getOrDefault("baseXpPerCorrect", "10"), "game");
		systemSettingService.save(SystemSettingService.STREAK_BONUS_XP, params.getOrDefault("streakBonusXp", "5"),
				"game");
		systemSettingService.save(SystemSettingService.ANNOUNCEMENT, params.getOrDefault("announcement", ""),
				"content");
		systemSettingService.save(SystemSettingService.MAINTENANCE_BANNER, params.getOrDefault("maintenanceBanner", ""),
				"content");
		systemSettingService.save(SystemSettingService.BRANDING_TAGLINE, params.getOrDefault("brandingTagline", ""),
				"branding");

		adminAuditService.log(principal, "UPDATE_SETTINGS", "SYSTEM", null, "Admin updated command center settings");
		redirectAttributes.addFlashAttribute("success", "System settings updated.");
		return "redirect:/admin/settings";
	}

	@GetMapping("/settings")
	public String settings(Model model, Principal principal) {
		addAdminContext(model, principal, "settings");
		model.addAttribute("questionCount", questionRepository.count());
		model.addAttribute("userCount", userRepository.count());
		model.addAttribute("resultCount", quizResultRepository.count());
		model.addAttribute("quizSettings", systemSettingService.getCategorySettings("quiz"));
		model.addAttribute("gameSettings", systemSettingService.getCategorySettings("game"));
		model.addAttribute("contentSettings", systemSettingService.getCategorySettings("content"));
		model.addAttribute("brandingSettings", systemSettingService.getCategorySettings("branding"));
		model.addAttribute("recentAdminActions", adminActionLogRepository.findTop10ByOrderByCreatedAtDesc());
		return "/admin/settings";
	}

	@GetMapping("/audit")
	public String audit(Model model, Principal principal) {
		addAdminContext(model, principal, "settings");
		model.addAttribute("logs", adminActionLogRepository.findTop20ByOrderByCreatedAtDesc());
		return "/admin/audit";
	}

	@GetMapping("/data/export/{type}")
	public ResponseEntity<byte[]> exportData(@PathVariable String type) throws IOException {
		Object payload = switch (type) {
		case "users" -> userRepository.findAll();
		case "results" -> quizResultRepository.findAll();
		case "questions" -> questionRepository.findAll();
		default -> List.of();
		};

		byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload)
				.getBytes(StandardCharsets.UTF_8);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=javajolt-" + type + ".json")
				.contentType(MediaType.APPLICATION_JSON).body(json);
	}

	private void addAdminContext(Model model, Principal principal, String activePage) {
		UserResponseDTO currentAdmin = userService.getByEmail(principal.getName());
		model.addAttribute("activePage", activePage);
		model.addAttribute("firstName", currentAdmin.firstName());
		model.addAttribute("lastName", currentAdmin.lastName());
		model.addAttribute("email", currentAdmin.email());
	}
}
