package com.daniella.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.daniella.entity.User;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserRepository;

@Service
public class GamificationService {

    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final SystemSettingService systemSettingService;

    public GamificationService(UserRepository userRepository,
                               QuizResultRepository quizResultRepository,
                               SystemSettingService systemSettingService) {
        this.userRepository = userRepository;
        this.quizResultRepository = quizResultRepository;
        this.systemSettingService = systemSettingService;
    }

    public int rewardUser(User user, int score, int totalQuestions, LocalDate playedDay) {
        int baseXpPerCorrect = systemSettingService.getInt(SystemSettingService.BASE_XP_PER_CORRECT, 10);
        int streakBonusXp = systemSettingService.getInt(SystemSettingService.STREAK_BONUS_XP, 5);
        int xpEarned = score * baseXpPerCorrect;

        LocalDate yesterday = playedDay.minusDays(1);
        boolean playedYesterday = quizResultRepository.findByUser(user).stream()
                .anyMatch(result -> result.getCompletedAt() != null && result.getCompletedAt().toLocalDate().isEqual(yesterday));
        boolean playedTodayBefore = quizResultRepository.findByUser(user).stream()
                .anyMatch(result -> result.getCompletedAt() != null && result.getCompletedAt().toLocalDate().isEqual(playedDay));

        if (!playedTodayBefore) {
            if (playedYesterday) {
                user.setStreakCount(user.getStreakCount() + 1);
            } else {
                user.setStreakCount(1);
            }
        }

        xpEarned += Math.max(0, user.getStreakCount() - 1) * streakBonusXp;
        user.setXp(user.getXp() + xpEarned);
        user.setRankTitle(resolveRank(user.getXp()));
        userRepository.save(user);
        return xpEarned;
    }

    public String resolveRank(int xp) {
        if (xp >= 1200) {
            return "LEGEND";
        }
        if (xp >= 800) {
            return "MASTER";
        }
        if (xp >= 450) {
            return "BREWER";
        }
        if (xp >= 200) {
            return "SPARK";
        }
        return "ROOKIE";
    }

    public int progressToNextRank(int xp) {
        int nextThreshold = nextThreshold(xp);
        int previousThreshold = previousThreshold(xp);
        if (nextThreshold == previousThreshold) {
            return 100;
        }
        return (int) (((xp - previousThreshold) * 100.0) / (nextThreshold - previousThreshold));
    }

    public int xpToNextRank(int xp) {
        int nextThreshold = nextThreshold(xp);
        return Math.max(0, nextThreshold - xp);
    }

    public Map<String, Integer> leaderboardSlice() {
        Map<String, Integer> board = new LinkedHashMap<>();
        userRepository.findTop10ByOrderByXpDesc()
                .forEach(user -> board.put(user.getFirstName() + " " + user.getLastName(), user.getXp()));
        return board;
    }

    private int previousThreshold(int xp) {
        if (xp >= 1200) {
            return 1200;
        }
        if (xp >= 800) {
            return 800;
        }
        if (xp >= 450) {
            return 450;
        }
        if (xp >= 200) {
            return 200;
        }
        return 0;
    }

    private int nextThreshold(int xp) {
        if (xp < 200) {
            return 200;
        }
        if (xp < 450) {
            return 450;
        }
        if (xp < 800) {
            return 800;
        }
        if (xp < 1200) {
            return 1200;
        }
        return 1200;
    }
}
