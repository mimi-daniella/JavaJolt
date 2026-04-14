package com.daniella.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.daniella.security.CustomOAuth2UserService;
import com.daniella.security.RoleBasedLoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class ApplicationConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
	                                       RoleBasedLoginSuccessHandler successHandler,
	                                       UserDetailsService userDetailsService,
	                                       CustomOAuth2UserService oauth2UserService) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/", "/public/**", "/auth/**", "/css/**", "/js/**", "/images/**", "/error/**").permitAll()
	            .requestMatchers("/admin/**").hasRole("ADMIN")
	            .requestMatchers("/dashboard").authenticated()
	            .anyRequest().permitAll()
	        )
	        .authenticationProvider(authenticationProvider(userDetailsService))
	        .formLogin(login -> login
	            .loginPage("/auth/login")
	            .loginProcessingUrl("/auth/login")
	            .successHandler(successHandler)
	            .failureUrl("/auth/login?error=true")
	            .permitAll()
	        )
	        .oauth2Login(oauth2 -> oauth2
	            .loginPage("/auth/login")
	            .userInfoEndpoint(userInfo -> userInfo
	     
	                .oidcUserService(oauth2UserService)
	            )
	            .successHandler(successHandler)
	            .failureUrl("/auth/login?error=true")
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
}