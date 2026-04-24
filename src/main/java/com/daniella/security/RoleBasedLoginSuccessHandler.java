package com.daniella.security;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.daniella.entity.User;
import com.daniella.repository.UserRepository;

@Component
public class RoleBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String contextPath = request.getContextPath();
        String redirectUrl = contextPath + "/dashboard"; // Default fallback
        
        Object principal = authentication.getPrincipal();
        String status = "";
        boolean isVerified = true; //default
        String userEmail = "";

        // Identify status based on login type
        if (principal instanceof CustomUserDetails userDetails) {
            status = userDetails.getStatus();
            isVerified = userDetails.isVerified();
            userEmail = userDetails.getUsername();
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                status = userOpt.get().getStatus() != null ? userOpt.get().getStatus().name() : "ACTIVE";
                isVerified = userOpt.get().isVerified();
            }
        }

        // Suspension Check
        if ("SUSPENDED".equalsIgnoreCase(status)) {
            redirectUrl = contextPath + "/auth/suspended";
        } 
        // Email Verification Check 
        else if (principal instanceof CustomUserDetails && !isVerified) {
        	 
            redirectUrl = contextPath + "/auth/login?unverified=true&email=" + userEmail;
        } 
        //Role-Based Redirection for Active & Verified Users
        else {
            if (principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null && user.getRole() != null) {
                    redirectUrl = contextPath + (user.getRole().name().equalsIgnoreCase("ADMIN") ? "/admin/dashboard" : "/dashboard");
                }
            } else {
                redirectUrl = contextPath + (authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ADMIN"))
                        ? "/admin/dashboard"
                        : "/dashboard");
            }
        }

        response.sendRedirect(redirectUrl);
    }
    }


