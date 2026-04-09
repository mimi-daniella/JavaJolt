package com.daniella.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.daniella.dto.UserResponseDTO;
import com.daniella.service.UserService;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        UserResponseDTO user = userService.getByEmail(principal.getName());
        model.addAttribute("user", user);
        return "auth/user-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        UserResponseDTO user = userService.getByEmail(principal.getName());
        model.addAttribute("user", user);
        return "auth/admin-dashboard";
    }
}
