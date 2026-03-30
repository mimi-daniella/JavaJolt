package com.daniella.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(@NotBlank String firstName, @NotBlank String lastName,
		@NotBlank @Email String email) {
}
