package com.daniella.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.daniella.entity.User;

public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (user.getRole() == null) {
			return List.of();
		}
		return List.of(new SimpleGrantedAuthority(user.getRole().name()));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public String getDisplayName() {
		return String.format("%s %s", user.getFirstName(), user.getLastName()).trim();
	}

	public String getStatus() {
		return (user != null && user.getStatus() != null) ? user.getStatus().name() : "ACTIVE";
	}

	public boolean isVerified() {
		return user.isVerified();
	}
}
