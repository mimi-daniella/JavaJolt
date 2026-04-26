package com.daniella.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.daniella.security.CustomAuthenticationFailureHandler;
import com.daniella.security.CustomOAuth2UserService;
import com.daniella.security.RoleBasedLoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class ApplicationConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
	                                       RoleBasedLoginSuccessHandler successHandler,
                                       CustomAuthenticationFailureHandler failureHandler,
	                                       UserDetailsService userDetailsService,
	                                       CustomOAuth2UserService oauth2UserService) throws Exception {
	    http
	        .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/feedback/submit")
            )
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/", "/public/**", "/auth/**", "/css/**", "/js/**", "/images/**", "/error/**").permitAll()
	            .requestMatchers("/api/feedback/submit").permitAll()
	            .requestMatchers("/api/feedback/all").hasRole("ADMIN")
	            .requestMatchers("/admin/**").hasRole("ADMIN")
	            .requestMatchers("/dashboard", "/dashboard/**", "/quiz/**", "/quizzes").authenticated()
	            .anyRequest().permitAll()
	        )
	        .sessionManagement(session -> session
	        		.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
	        	
	        		)
	        .authenticationProvider(authenticationProvider(userDetailsService))
	        .formLogin(login -> login
	            .loginPage("/auth/login")
	            .loginProcessingUrl("/auth/login")
	            .successHandler(successHandler)
	            .failureHandler(failureHandler)
	            .permitAll()
	        )
	        .oauth2Login(oauth2 -> oauth2
	            .loginPage("/auth/login")
	            .userInfoEndpoint(userInfo -> userInfo
	     
	                .oidcUserService(oauth2UserService)
	            )
	            .successHandler(successHandler)
	            .failureHandler(failureHandler)
	        )
	        .logout(logout -> logout
	            .logoutUrl("/logout")
	            .logoutSuccessUrl("/auth/login?logout=true")
	            .permitAll()
	        );
	    return http.build();
	}


    
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
