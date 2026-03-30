package com.daniella.dto;

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

	}


