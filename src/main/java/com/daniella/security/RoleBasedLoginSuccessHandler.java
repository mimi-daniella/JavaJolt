package com.daniella.security;

import java.io.IOException;

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
        String redirectUrl = contextPath + "/dashboard"; // default

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            // OAuth2 login
            String email = oauth2User.getAttribute("email");
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getRole() != null) {
                redirectUrl = contextPath + (user.getRole().name().equals("ADMIN") ? "/admin/dashboard" : "/dashboard");
            }
        } else {
            // Regular form login
            redirectUrl = contextPath + (authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals)
                    ? "/admin/dashboard"
                    : "/dashboard");
        }

        response.sendRedirect(redirectUrl);
    }
}
