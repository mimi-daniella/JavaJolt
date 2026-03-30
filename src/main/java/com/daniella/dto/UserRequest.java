package com.daniella.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {

	@NotBlank(message = "Firstname is required")
	@Size(min = 2, max = 15)
	private String firstName;
	
	
	@NotBlank(message = "Lastname is required")
	@Size(min = 2, max = 15)
	private String lastName;
	
	
	@NotBlank(message = "Email is required")
	@Size(min = 2, max = 150)
	@Email(message = "Enter a valid email")
	private String email;
	
	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String password;
	
	private String confirmPassword;
	
	
	
//Getters & setters
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	
	
}
