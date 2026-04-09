package com.daniella.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.daniella.entity.User;
import com.daniella.enums.Role;
import com.daniella.repository.UserRepository;
import com.daniella.service.UserService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            // Create new user
            User newUser = new User();
            newUser.setFirstName(name != null ? name : email);
            newUser.setLastName("");
            newUser.setEmail(email);
            newUser.setRole(Role.ROLE_CLIENT); // Default to CLIENT role
            newUser.setPassword(""); // OAuth2 users don't have passwords
            userRepository.save(newUser);
        }

        return oauth2User;
    }
}