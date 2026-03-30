package com.daniella.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeDTO(
	@NotBlank String oldPassword,
	@NotBlank String newPassword,
	@NotBlank String confirmPassword
	) {}