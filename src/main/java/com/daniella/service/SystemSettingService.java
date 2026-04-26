package com.daniella.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.daniella.entity.SystemSetting;
import com.daniella.repository.SystemSettingRepository;

@Service
public class SystemSettingService {

    public static final String QUESTIONS_PER_ROUND = "quiz.questionsPerRound";
    public static final String TIMER_ENABLED = "quiz.timerEnabled";
    public static final String PASS_MARK = "quiz.passMark";
    public static final String ALLOW_RETAKES = "quiz.allowRetakes";
    public static final String DAILY_CHALLENGE_ENABLED = "game.dailyChallengeEnabled";
    public static final String FEATURED_CHALLENGE_TITLE = "game.featuredChallengeTitle";
    public static final String DAILY_CHALLENGE_CATEGORY = "game.dailyChallengeCategory";
    public static final String BASE_XP_PER_CORRECT = "game.baseXpPerCorrect";
    public static final String STREAK_BONUS_XP = "game.streakBonusXp";
    public static final String ANNOUNCEMENT = "content.announcement";
    public static final String MAINTENANCE_BANNER = "content.maintenanceBanner";
    public static final String BRANDING_TAGLINE = "branding.tagline";
    public static final String DEFAULT_CATEGORY = "quiz.defaultCategory";
    public static final String DEFAULT_DIFFICULTY = "quiz.defaultDifficulty";

    private final SystemSettingRepository systemSettingRepository;

    public SystemSettingService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    public void ensureDefaults() {
        saveIfMissing(QUESTIONS_PER_ROUND, "10", "quiz");
        saveIfMissing(TIMER_ENABLED, "false", "quiz");
        saveIfMissing(PASS_MARK, "60", "quiz");
        saveIfMissing(ALLOW_RETAKES, "true", "quiz");
        saveIfMissing(DEFAULT_CATEGORY, "", "quiz");
        saveIfMissing(DEFAULT_DIFFICULTY, "", "quiz");
        saveIfMissing(DAILY_CHALLENGE_ENABLED, "true", "game");
        saveIfMissing(FEATURED_CHALLENGE_TITLE, "Barista Boss Battle", "game");
        saveIfMissing(DAILY_CHALLENGE_CATEGORY, "Java", "game");
        saveIfMissing(BASE_XP_PER_CORRECT, "10", "game");
        saveIfMissing(STREAK_BONUS_XP, "5", "game");
        saveIfMissing(ANNOUNCEMENT, "Welcome to JavaJolt Command Center.", "content");
        saveIfMissing(MAINTENANCE_BANNER, "", "content");
        saveIfMissing(BRANDING_TAGLINE, "Brew your Java skills, one quiz at a time.", "branding");
    }

    public String get(String key, String fallback) {
        return systemSettingRepository.findById(key)
                .map(SystemSetting::getSettingValue)
                .orElse(fallback);
    }

    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, String.valueOf(fallback)));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public boolean getBoolean(String key, boolean fallback) {
        return Boolean.parseBoolean(get(key, String.valueOf(fallback)));
    }

    public void save(String key, String value, String category) {
        SystemSetting setting = systemSettingRepository.findById(key)
                .orElseGet(() -> new SystemSetting(key, value, category));
        setting.setSettingValue(value);
        setting.setCategory(category);
        systemSettingRepository.save(setting);
    }

    public Map<String, String> getCategorySettings(String category) {
        List<SystemSetting> settings = systemSettingRepository.findByCategoryOrderBySettingKeyAsc(category);
        Map<String, String> values = new LinkedHashMap<>();
        for (SystemSetting setting : settings) {
            values.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return values;
    }

    private void saveIfMissing(String key, String value, String category) {
        if (!systemSettingRepository.existsById(key)) {
            systemSettingRepository.save(new SystemSetting(key, value, category));
        }
    }
}
