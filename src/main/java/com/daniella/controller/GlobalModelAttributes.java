package com.daniella.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.daniella.repository.UserRepository;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Shared frontend state so navbar and public pages can react to logged-in users.
    @ModelAttribute("loggedIn")
    public boolean loggedIn(Principal principal) {
        return principal != null;
    }

    @ModelAttribute("currentUserFirstName")
    public String currentUserFirstName(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userRepository.findByEmail(principal.getName())
                .map(user -> user.getFirstName())
                .orElse(principal.getName());
    }
}
