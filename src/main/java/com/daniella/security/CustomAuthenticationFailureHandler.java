package com.daniella.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String contextPath = request.getContextPath();
        String username = request.getParameter("username");

        if (exception instanceof DisabledException && username != null && !username.isBlank()) {
            response.sendRedirect(contextPath + "/auth/login?unverified=true&email="
                    + URLEncoder.encode(username, StandardCharsets.UTF_8));
            return;
        }

        if (exception instanceof LockedException) {
            response.sendRedirect(contextPath + "/auth/suspended");
            return;
        }

        if (exception instanceof OAuth2AuthenticationException oauthEx
                && "account_suspended".equals(oauthEx.getError().getErrorCode())) {
            response.sendRedirect(contextPath + "/auth/suspended");
            return;
        }

        response.sendRedirect(contextPath + "/auth/login?error=true");
    }
}
