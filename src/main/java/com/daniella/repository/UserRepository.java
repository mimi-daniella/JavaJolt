package com.daniella.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.User;
import com.daniella.enums.Role;
import com.daniella.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	long count();
	long countByStatus(UserStatus status);
	long countByRole(Role role);
	long countByIsVerified(boolean isVerified);
	java.util.List<User> findTop10ByOrderByXpDesc();
}
