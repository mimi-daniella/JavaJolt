package com.daniella.security;

import java.io.IOException;
import java.time.LocalDateTime;

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
        Object principal = authentication.getPrincipal();

        User user = null;
        if (principal instanceof CustomUserDetails userDetails) {
            user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
            }
        }

        if (user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        String redirectUrl = contextPath + (authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ADMIN"))
                ? "/admin/dashboard"
                : "/dashboard");

        response.sendRedirect(redirectUrl);
    }
}
