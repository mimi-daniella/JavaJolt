package com.daniella.controller;

import com.daniella.dto.UserRequest;
import com.daniella.entity.User;
import com.daniella.exception.BusinessException;
import com.daniella.repository.UserRepository;
import com.daniella.service.EmailService;
import com.daniella.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.security.SecureRandom;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String OtpKey = "PASSWORD_RESET_OTP";
    private static final String OtpEmailKey = "PASSWORD_RESET_EMAIL";
    private static final String OtpVerifiedKey = "PASSWORD_RESET_VERIFIED";
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRequest") UserRequest userRequest,
                               BindingResult bindingResult,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.createUser(userRequest);

            String otp = generateOtp();
            session.setAttribute(OtpKey, otp);
            session.setAttribute(OtpEmailKey, userRequest.getEmail());
            session.setAttribute(OtpVerifiedKey, false);
            session.setAttribute("OTP_SENT_AT", Instant.now());
            session.setAttribute("IS_REGISTRATION", true);

            try {
                emailService.sendOtpEmail(userRequest.getEmail(), otp);
            } catch (MailException | MessagingException ex) {
                logger.error("Failed to send OTP email to {}", userRequest.getEmail(), ex);
                clearOtpSession(session);
                model.addAttribute("error", "Unable to send verification email. Please check your settings.");
                return "auth/register";
            }

            redirectAttributes.addFlashAttribute("info", "A verification code has been sent to your email.");
            return "redirect:/auth/verify-otp?mode=register";

        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String registered,
                        @RequestParam(required = false) String reset,
                        @RequestParam(required = false) String unverified,
                        @RequestParam(required = false) String suspended,
                        @RequestParam(required = false) String email,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (suspended != null) {
            model.addAttribute("error", "This account is suspended.");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out.");
        }
        if (registered != null) {
            model.addAttribute("success", "Account verified successfully! Please log in.");
        }
        if (reset != null) {
            model.addAttribute("success", "Password updated. You may now log in.");
        }
        
        // Handle unverified popup logic
        if (unverified != null) {
            model.addAttribute("unverified", true);
            model.addAttribute("unverifiedEmail", email);
        }
        
        return "auth/login";
    }

    @GetMapping("/verify-email")
    public String sendVerificationEmail(@RequestParam String email, HttpSession session, RedirectAttributes ra) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Email not found.");
            return "redirect:/auth/login";
        }

        if (userOpt.get().isVerified()) {
            ra.addFlashAttribute("info", "This account is already verified.");
            return "redirect:/auth/login";
        }

        String otp = generateOtp();
        session.setAttribute(OtpKey, otp);
        session.setAttribute(OtpEmailKey, email);
        session.setAttribute(OtpVerifiedKey, false);
        session.setAttribute("OTP_SENT_AT", Instant.now());
        session.setAttribute("IS_REGISTRATION", true);

        try {
            emailService.sendOtpEmail(email, otp);
            ra.addFlashAttribute("info", "A new verification code has been sent!");
            return "redirect:/auth/verify-otp?mode=register";
        } catch (Exception ex) {
            logger.error("Failed to resend verification email", ex);
            ra.addFlashAttribute("error", "Could not send email. Try again later.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendResetOtp(@RequestParam String email,
                               Model model,
                               HttpSession session) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "We couldn't find an account with that email.");
            return "auth/forgot-password";
        }

        String otp = generateOtp();
        session.setAttribute(OtpKey, otp);
        session.setAttribute(OtpEmailKey, email);
        session.setAttribute(OtpVerifiedKey, false);
        session.setAttribute("OTP_SENT_AT", Instant.now());
        session.setAttribute("IS_REGISTRATION", false);

        try {
            emailService.sendOtpEmail(email, otp);
        } catch (MailException | MessagingException ex) {
            logger.error("Failed to reset OTP email", ex);
            clearOtpSession(session);
            model.addAttribute("error", "Unable to send reset email.");
            return "auth/forgot-password";
        }

        model.addAttribute("info", "A verification code has been sent to your email.");
        return "redirect:/auth/verify-otp?mode=reset";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session) {
        if (session.getAttribute(OtpKey) == null) {
            return "redirect:/auth/forgot-password";
        }
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String code,
                            HttpSession session,
                            Model model) {
        Object expected = session.getAttribute(OtpKey);
        String email = (String) session.getAttribute(OtpEmailKey);
        Instant sentAt = (Instant) session.getAttribute("OTP_SENT_AT");

        if (sentAt == null || sentAt.plus(OTP_TTL).isBefore(Instant.now())) {
            clearOtpSession(session);
            model.addAttribute("error", "The code has expired. Please request a new one.");
            return "auth/verify-otp";
        }

        if (expected == null || !expected.toString().equals(code.trim())) {
            model.addAttribute("error", "The code is invalid. Please try again.");
            return "auth/verify-otp";
        }

        session.setAttribute(OtpVerifiedKey, true);

        boolean isRegistration = Boolean.TRUE.equals(session.getAttribute("IS_REGISTRATION"));
        if (isRegistration) {
            // Update the user's verification status in the database
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setVerified(true);
                userRepository.save(user);
            });
            clearOtpSession(session);
            return "redirect:/auth/login?registered=true";
        }

        return "redirect:/auth/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(OtpVerifiedKey))) {
            return "redirect:/auth/forgot-password";
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String saveNewPassword(@RequestParam String password,
                                  @RequestParam String confirmPassword,
                                  HttpSession session,
                                  Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "auth/reset-password";
        }

        String email = (String) session.getAttribute(OtpEmailKey);
        if (email == null) {
            return "redirect:/auth/forgot-password";
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Unable to update password.");
            return "auth/reset-password";
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        clearOtpSession(session);

        return "redirect:/auth/login?reset=true";
    }

    @GetMapping("/suspended")
    public String suspendedPage() {
        return "auth/suspended";
    }

    private void clearOtpSession(HttpSession session) {
        session.removeAttribute(OtpKey);
        session.removeAttribute(OtpEmailKey);
        session.removeAttribute(OtpVerifiedKey);
        session.removeAttribute("OTP_SENT_AT");
        session.removeAttribute("IS_REGISTRATION");
    }

    private String generateOtp() {
        return String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
    }
}
