package com.daniella.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

@Entity
public class Feedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;

	@Column(length = 2000)
	private String message;

	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	// Constructors
	public Feedback() {
	}

	public Feedback(String email, String message) {
		this.email = email;
		this.message = message;
		this.createdAt = LocalDateTime.now(); // ✅ add timestamp
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) { // ✅ added
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getCreatedAt() { // ✅ added
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) { // ✅ added
		this.createdAt = createdAt;
	}
}
