package com.daniella.dto;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.daniella.entity.User;

public record UserResponseDTO (
			Long id,
			String firstName,
			String lastName,
			String email,
			String role
			) {

		public static UserResponseDTO fromEntity(User user) {
			return new UserResponseDTO(
					user.getId(),
					user.getFirstName(),
					user.getLastName(),
					user.getEmail(),
					user.getRole() != null ? user.getRole().name() : null
					);
		}

		public boolean hasRole(String role) {
		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		    if (auth == null || auth.getAuthorities() == null) {
		        return false;
		    }
		    return auth.getAuthorities().stream()
		               .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
		}

	}


