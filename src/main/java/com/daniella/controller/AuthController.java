package com.daniella.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.daniella.dto.UserRequest;
import com.daniella.entity.User;
import com.daniella.exception.BusinessException;
import com.daniella.repository.UserRepository;
import com.daniella.service.EmailService;
import com.daniella.service.UserService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final String OtpKey = "PASSWORD_RESET_OTP";
    private static final String OtpEmailKey = "PASSWORD_RESET_EMAIL";
    private static final String OtpVerifiedKey = "PASSWORD_RESET_VERIFIED";

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
            
            // Generate OTP and store in session for email verification
            String otp = String.format("%06d", new Random().nextInt(1_000_000));
            session.setAttribute(OtpKey, otp);
            session.setAttribute(OtpEmailKey, userRequest.getEmail());
            session.setAttribute(OtpVerifiedKey, false);
            session.setAttribute("OTP_SENT_AT", Instant.now());
            session.setAttribute("IS_REGISTRATION", true);
            
            // Send OTP email
            try {
                emailService.sendOtpEmail(userRequest.getEmail(), otp);
            } catch (MailException ex) {
                session.removeAttribute(OtpKey);
                session.removeAttribute(OtpEmailKey);
                session.removeAttribute(OtpVerifiedKey);
                session.removeAttribute("OTP_SENT_AT");
                session.removeAttribute("IS_REGISTRATION");
                model.addAttribute("error", "Unable to send verification email. Please check your email settings and try again.");
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
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out.");
        }
        if (registered != null) {
            model.addAttribute("success", "Account created successfully. Please log in.");
        }
        if (reset != null) {
            model.addAttribute("success", "Password updated. You may now log in.");
        }
        return "auth/login";
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

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        session.setAttribute(OtpKey, otp);
        session.setAttribute(OtpEmailKey, email);
        session.setAttribute(OtpVerifiedKey, false);
        session.setAttribute("OTP_SENT_AT", Instant.now());

        // Send OTP email
        try {
            emailService.sendOtpEmail(email, otp);
        } catch (MailException ex) {
            session.removeAttribute(OtpKey);
            session.removeAttribute(OtpEmailKey);
            session.removeAttribute(OtpVerifiedKey);
            session.removeAttribute("OTP_SENT_AT");
            model.addAttribute("error", "Unable to send verification email. Please check your email settings and try again.");
            return "auth/forgot-password";
        }

        model.addAttribute("info", "A verification code has been sent to your email.");
        return "redirect:/auth/verify-otp?mode=reset";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model) {
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
        if (expected == null || !expected.toString().equals(code.trim())) {
            model.addAttribute("error", "The code is invalid. Please try again.");
            return "auth/verify-otp";
        }
        session.setAttribute(OtpVerifiedKey, true);
        
        // If this is registration flow, go to login; otherwise go to reset password
        boolean isRegistration = Boolean.TRUE.equals(session.getAttribute("IS_REGISTRATION"));
        if (isRegistration) {
            session.removeAttribute("IS_REGISTRATION");
            session.removeAttribute(OtpKey);
            session.removeAttribute(OtpEmailKey);
            session.removeAttribute(OtpVerifiedKey);
            return "redirect:/auth/login?registered=true";
        }
        
        return "redirect:/auth/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(HttpSession session, Model model) {
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
            model.addAttribute("error", "Unable to update password for this account.");
            return "auth/reset-password";
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        session.removeAttribute(OtpKey);
        session.removeAttribute(OtpEmailKey);
        session.removeAttribute(OtpVerifiedKey);
        session.removeAttribute("OTP_SENT_AT");

        return "redirect:/auth/login?reset=true";
    }
}
