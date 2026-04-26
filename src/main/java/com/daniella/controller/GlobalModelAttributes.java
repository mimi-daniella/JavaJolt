package com.daniella.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.daniella.repository.UserRepository;
import com.daniella.service.UserService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final UserService userService;

    public GlobalModelAttributes(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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

    @ModelAttribute("currentUserAvatarPath")
    public String currentUserAvatarPath(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userRepository.findByEmail(principal.getName())
                .map(user -> userService.resolveAvatarPath(user.getEmail(), user.getAvatarPath()))
                .orElse(null);
    }
}
