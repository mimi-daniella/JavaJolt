package com.daniella.security;

import com.daniella.entity.User;
import com.daniella.enums.Role;
import com.daniella.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch user from Google
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        
        // 1. Find or Register User
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            // Get names from Google OIDC claims
            newUser.setFirstName(oidcUser.getGivenName() != null ? oidcUser.getGivenName() : "Google");
            newUser.setLastName(oidcUser.getFamilyName() != null ? oidcUser.getFamilyName() : "User");
            newUser.setRole(Role.ROLE_CLIENT);
            // Set random password to satisfy @NotBlank constraint
            newUser.setPassword(UUID.randomUUID().toString());
            return userRepository.save(newUser);
        });

        // 2. Return OidcUser with authorities and email as principal name
        return new DefaultOidcUser(
            Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
            oidcUser.getIdToken(),
            oidcUser.getUserInfo(),
            "email"
        );
    }
}
