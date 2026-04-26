package com.daniella.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.daniella.service.SystemSettingService;

@Component
public class SystemSettingSeeder implements CommandLineRunner {

    private final SystemSettingService systemSettingService;

    public SystemSettingSeeder(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @Override
    public void run(String... args) {
        systemSettingService.ensureDefaults();
    }
}
