package com.daniella.admin;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.daniella.dto.UserRequest;
import com.daniella.dto.UserResponseDTO;
import com.daniella.entity.QuizResult;
import com.daniella.entity.User;
import com.daniella.enums.Role;
import com.daniella.enums.UserStatus;
import com.daniella.repository.QuizResultRepository;
import com.daniella.repository.UserRepository;
import com.daniella.service.AdminAuditService;
import com.daniella.service.UserService;
import com.daniella.util.AvatarCatalog;

@Controller
@RequestMapping("/admin/users")
public class UserManagement {

    private final UserRepository userRepository;
    private final UserService userService;
    private final QuizResultRepository quizResultRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService adminAuditService;

    public UserManagement(UserRepository userRepository,
                          UserService userService,
                          QuizResultRepository quizResultRepository,
                          PasswordEncoder passwordEncoder,
                          AdminAuditService adminAuditService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.quizResultRepository = quizResultRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminAuditService = adminAuditService;
    }

    @GetMapping
    public String users(@RequestParam(defaultValue = "") String search,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String role,
                        @RequestParam(required = false) String verification,
                        Model model,
                        Principal principal) {
        UserResponseDTO currentAdmin = userService.getByEmail(principal.getName());
        List<User> allUsers = userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> normalizedSearch.isBlank()
                        || (user.getFirstName() + " " + user.getLastName()).toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || user.getEmail().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .filter(user -> status == null || status.isBlank() || user.getStatus().name().equalsIgnoreCase(status))
                .filter(user -> role == null || role.isBlank() || (user.getRole() != null && user.getRole().name().equalsIgnoreCase(role)))
                .filter(user -> verification == null || verification.isBlank()
                        || ("VERIFIED".equalsIgnoreCase(verification) && user.isVerified())
                        || ("UNVERIFIED".equalsIgnoreCase(verification) && !user.isVerified()))
                .collect(Collectors.toList());

        model.addAttribute("activePage", "users");
        model.addAttribute("firstName", currentAdmin.firstName());
        model.addAttribute("lastName", currentAdmin.lastName());
        model.addAttribute("email", currentAdmin.email());
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("activeUsers", allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count());
        model.addAttribute("suspendedUsers", allUsers.stream().filter(u -> u.getStatus() == UserStatus.SUSPENDED).count());
        model.addAttribute("adminUsers", allUsers.stream().filter(u -> u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_SUPERADMIN).count());
        model.addAttribute("verifiedUsers", allUsers.stream().filter(User::isVerified).count());
        model.addAttribute("users", filteredUsers);
        model.addAttribute("currentAdminId", currentAdmin.id());
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedVerification", verification);
        model.addAttribute("roles", Role.values());
        model.addAttribute("statuses", UserStatus.values());

        return "admin/users";
    }

    @GetMapping("/new")
    public String newUser(Model model, Principal principal) {
        addAdminContext(model, principal, "users");
        UserRequest userRequest = new UserRequest();
        model.addAttribute("userRequest", userRequest);
        model.addAttribute("roles", List.of(Role.ROLE_CLIENT, Role.ROLE_ADMIN));
        model.addAttribute("statuses", UserStatus.values());
        return "admin/new-user";
    }

    @PostMapping("/new")
    public String saveUser(@ModelAttribute UserRequest userRequest,
                           @RequestParam(defaultValue = "ROLE_CLIENT") Role role,
                           @RequestParam(defaultValue = "ACTIVE") UserStatus status,
                           @RequestParam(defaultValue = "false") boolean verified,
                           Principal principal,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("userRequest", userRequest);
            model.addAttribute("roles", List.of(Role.ROLE_CLIENT, Role.ROLE_ADMIN));
            model.addAttribute("statuses", UserStatus.values());
            addAdminContext(model, principal, "users");
            return "admin/new-user";
        }
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already registered.");
            model.addAttribute("userRequest", userRequest);
            model.addAttribute("roles", List.of(Role.ROLE_CLIENT, Role.ROLE_ADMIN));
            model.addAttribute("statuses", UserStatus.values());
            addAdminContext(model, principal, "users");
            return "admin/new-user";
        }

        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(role);
        user.setStatus(status);
        user.setVerified(verified);
        user.setAvatarPath(AvatarCatalog.defaultAvatarFor(userRequest.getEmail()));
        userRepository.save(user);

        adminAuditService.log(principal, "CREATE_USER", "USER", String.valueOf(user.getId()), user.getEmail());
        redirectAttributes.addFlashAttribute("success", "User created successfully.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model, Principal principal) {
        addAdminContext(model, principal, "users");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        List<QuizResult> results = quizResultRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(QuizResult::getCompletedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        model.addAttribute("selectedUser", user);
        model.addAttribute("attemptCount", results.size());
        model.addAttribute("averageScore", results.stream().mapToInt(QuizResult::getScore).average().orElse(0.0));
        model.addAttribute("latestResults", results.stream().limit(8).collect(Collectors.toList()));
        model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        return "admin/user-details";
    }

    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable Long id, Model model, Principal principal) {
        addAdminContext(model, principal, "users");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("roles", Arrays.asList(Role.values()));
        model.addAttribute("statuses", Arrays.asList(UserStatus.values()));
        return "admin/edit-user";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User formUser,
                             @RequestParam(defaultValue = "") String newPassword,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (userRepository.findByEmail(formUser.getEmail())
                .filter(found -> !found.getId().equals(existing.getId()))
                .isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Email already registered.");
            return "redirect:/admin/users/" + id + "/edit";
        }
        existing.setFirstName(formUser.getFirstName());
        existing.setLastName(formUser.getLastName());
        existing.setEmail(formUser.getEmail());
        existing.setRole(formUser.getRole());
        existing.setStatus(formUser.getStatus());
        existing.setVerified(formUser.isVerified());
        if (!newPassword.isBlank()) {
            existing.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(existing);

        adminAuditService.log(principal, "UPDATE_USER", "USER", String.valueOf(existing.getId()), existing.getEmail());
        redirectAttributes.addFlashAttribute("success", "User updated successfully.");
        return "redirect:/admin/users/" + existing.getId();
    }

    @PostMapping("/{id}/verify")
    public String verifyUser(@PathVariable Long id, Principal principal) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        userRepository.save(user);
        adminAuditService.log(principal, "VERIFY_USER", "USER", String.valueOf(id), user.getEmail());
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unverify")
    public String unverifyUser(@PathVariable Long id, Principal principal) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(false);
        userRepository.save(user);
        adminAuditService.log(principal, "UNVERIFY_USER", "USER", String.valueOf(id), user.getEmail());
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/suspend")
    public String suspendUser(@PathVariable Long id, Principal principal) {
        userService.suspendUser(id);
        adminAuditService.log(principal, "SUSPEND_USER", "USER", String.valueOf(id), "User suspended");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reactivate")
    public String reactivateUser(@PathVariable Long id, Principal principal) {
        userService.reactivateUser(id);
        adminAuditService.log(principal, "REACTIVATE_USER", "USER", String.valueOf(id), "User reactivated");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, Principal principal) {
        userService.deleteUser(id);
        adminAuditService.log(principal, "DELETE_USER", "USER", String.valueOf(id), "User deleted");
        return "redirect:/admin/users";
    }

    private void addAdminContext(Model model, Principal principal, String activePage) {
        UserResponseDTO currentAdmin = userService.getByEmail(principal.getName());
        model.addAttribute("activePage", activePage);
        model.addAttribute("firstName", currentAdmin.firstName());
        model.addAttribute("lastName", currentAdmin.lastName());
        model.addAttribute("email", currentAdmin.email());
    }
}
