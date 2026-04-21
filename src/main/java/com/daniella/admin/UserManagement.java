package com.daniella.admin;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.daniella.dto.UserResponseDTO;
import com.daniella.entity.User;
import com.daniella.repository.UserRepository;
import com.daniella.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class UserManagement {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserManagement(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public String users(Model model, Principal principal) {
        model.addAttribute("activePage", "users");
        
        // 1. Get all users as entities (to match your current logic)
        List<User> allUsers = userRepository.findAll();
        
        // 2. Get the current logged-in admin DTO
        UserResponseDTO currentAdmin = userService.getByEmail(principal.getName());

        // 3. Calculate statistics using Streams
        long totalUsersCount = allUsers.size();
        long activeUsersCount = allUsers.stream()
                .filter(u -> u.getStatus() != null && "ACTIVE".equalsIgnoreCase(u.getStatus().name()))
                .count();
        long suspendedUsersCount = allUsers.stream()
                .filter(u -> u.getStatus() != null && "SUSPENDED".equalsIgnoreCase(u.getStatus().name()))
                .count();
        long adminUsersCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "ROLE_ADMIN".equalsIgnoreCase(u.getRole().name()))
                .count();

        // 4. Populate the model
        model.addAttribute("firstName", currentAdmin.firstName());
        model.addAttribute("lastName", currentAdmin.lastName());
        model.addAttribute("email", currentAdmin.email());
        
        model.addAttribute("totalUsers", totalUsersCount);
        model.addAttribute("activeUsers", activeUsersCount);
        model.addAttribute("suspendedUsers", suspendedUsersCount);
        model.addAttribute("adminUsers", adminUsersCount);
        
        model.addAttribute("users", allUsers);
        model.addAttribute("currentAdminId", currentAdmin.id());

        return "admin/users";
    }


    @PostMapping("/{id}/suspend")
    public String suspendUser(@PathVariable Long id) {
        userService.suspendUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reactivate")
    public String reactivateUser(@PathVariable Long id) {
        userService.reactivateUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
