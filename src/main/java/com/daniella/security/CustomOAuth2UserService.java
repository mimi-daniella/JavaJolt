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
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);

            // Pull names from OIDC attributes
            String givenName = (String) oidcUser.getAttributes().get("given_name");
            String familyName = (String) oidcUser.getAttributes().get("family_name");

            newUser.setFirstName(givenName != null ? givenName : "Google");
            newUser.setLastName(familyName != null ? familyName : "User");

            newUser.setRole(Role.ROLE_CLIENT);
            newUser.setPassword(UUID.randomUUID().toString());
            newUser.setStatus(com.daniella.enums.UserStatus.ACTIVE);
            newUser.setVerified(true); 
            return userRepository.save(newUser);
        });

        return new DefaultOidcUser(
            Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
            oidcUser.getIdToken(),
            oidcUser.getUserInfo(),
            "email"
        );
    }
}
