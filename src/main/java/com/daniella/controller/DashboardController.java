package com.daniella.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.daniella.dto.PasswordChangeDTO;
import com.daniella.dto.UserResponseDTO;
import com.daniella.dto.UserUpdateDTO;
import com.daniella.entity.User;
import com.daniella.enums.Difficulty;
import com.daniella.exception.BusinessException;
import com.daniella.repository.QuestionRepository;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserRepository;
import com.daniella.service.GamificationService;
import com.daniella.service.SystemSettingService;
import com.daniella.service.UserService;
import com.daniella.util.AvatarCatalog;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final GamificationService gamificationService;
    private final SystemSettingService systemSettingService;

    public DashboardController(UserService userService,
                               UserRepository userRepository,
                               QuestionRepository questionRepository,
                               QuizResultRepository quizResultRepository,
                               GamificationService gamificationService,
                               SystemSettingService systemSettingService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.quizResultRepository = quizResultRepository;
        this.gamificationService = gamificationService;
        this.systemSettingService = systemSettingService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User userEntity = getCurrentUser(principal);
        UserResponseDTO user = userService.getByEmail(principal.getName());
        model.addAttribute("user", user);

        if (user.hasRole("ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("profileForm", new UserUpdateDTO(user.firstName(), user.lastName(), user.email()));
        model.addAttribute("passwordForm", new PasswordChangeDTO("", "", ""));
        model.addAttribute("statCards", buildStatCards(userEntity));
        model.addAttribute("activities", buildActivities(userEntity));
        model.addAttribute("xp", userEntity.getXp());
        model.addAttribute("rankTitle", userEntity.getRankTitle());
        model.addAttribute("streakCount", userEntity.getStreakCount());
        model.addAttribute("avatarPath", userService.resolveAvatarPath(userEntity.getEmail(), userEntity.getAvatarPath()));
        model.addAttribute("avatarChoices", AvatarCatalog.all());
        model.addAttribute("progressPercent", gamificationService.progressToNextRank(userEntity.getXp()));
        model.addAttribute("xpToNextRank", gamificationService.xpToNextRank(userEntity.getXp()));
        model.addAttribute("announcement", systemSettingService.get(SystemSettingService.ANNOUNCEMENT, ""));
        model.addAttribute("featuredChallengeTitle", systemSettingService.get(SystemSettingService.FEATURED_CHALLENGE_TITLE, "Barista Boss Battle"));
        model.addAttribute("dailyChallengeEnabled", systemSettingService.getBoolean(SystemSettingService.DAILY_CHALLENGE_ENABLED, true));
        model.addAttribute("dailyChallengeCategory", systemSettingService.get(SystemSettingService.DAILY_CHALLENGE_CATEGORY, "Java"));
        model.addAttribute("leaderboard", gamificationService.leaderboardSlice());
        return "user/dashboard";
    }

    @GetMapping("/settings")
    public String settings(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = getCurrentUser(principal);
        UserResponseDTO userDto = userService.getByEmail(principal.getName());
        model.addAttribute("user", userDto);
        model.addAttribute("profileForm", new UserUpdateDTO(userDto.firstName(), userDto.lastName(), userDto.email()));
        model.addAttribute("passwordForm", new PasswordChangeDTO("", "", ""));
        model.addAttribute("xp", user.getXp());
        model.addAttribute("rankTitle", user.getRankTitle());
        model.addAttribute("streakCount", user.getStreakCount());
        model.addAttribute("avatarPath", userService.resolveAvatarPath(user.getEmail(), user.getAvatarPath()));
        return "user/settings";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("profileForm") UserUpdateDTO updateDTO,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserDetails(principal.getName(), updateDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/password")
    public String updatePassword(@ModelAttribute("passwordForm") PasswordChangeDTO passwordDTO,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (!passwordDTO.newPassword().equals(passwordDTO.confirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/dashboard/settings";
        }

        try {
            userService.updatePassword(principal.getName(), passwordDTO);
            redirectAttributes.addFlashAttribute("success", "Password updated successfully.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard/settings";
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private List<Map<String, String>> buildStatCards(User user) {
        int easy = questionRepository.findByDifficulty(Difficulty.EASY).size();
        int medium = questionRepository.findByDifficulty(Difficulty.MEDIUM).size();
        int hard = questionRepository.findByDifficulty(Difficulty.HARD).size();
        int attempts = quizResultRepository.findByUser(user).size();

        return List.of(
                Map.of("label", "Easy Questions", "value", String.valueOf(easy), "accent", "yellow"),
                Map.of("label", "Medium Questions", "value", String.valueOf(medium), "accent", "brown"),
                Map.of("label", "Hard Questions", "value", String.valueOf(hard), "accent", "espresso"),
                Map.of("label", "Quiz Attempts", "value", String.valueOf(attempts), "accent", "yellow"));
    }

    private List<Map<String, String>> buildActivities(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - hh:mm a");

        return quizResultRepository.findByUser(user).stream()
                .sorted((a, b) -> {
                    LocalDateTime right = b.getCompletedAt() != null ? b.getCompletedAt() : LocalDateTime.MIN;
                    LocalDateTime left = a.getCompletedAt() != null ? a.getCompletedAt() : LocalDateTime.MIN;
                    return right.compareTo(left);
                })
                .limit(5)
                .map(result -> Map.of(
                        "title", "Quiz completed",
                        "meta", result.getCompletedAt() != null ? result.getCompletedAt().format(formatter) : "Recently",
                        "score", result.getScore() + "/" + result.getTotalQuestions() + " - +" + result.getXpEarned() + " XP"))
                .toList();
    }
}
