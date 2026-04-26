package com.daniella.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daniella.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping("/avatar")
	public ResponseEntity<String>  updateAvatar(@RequestBody Map<String, String> payload,
            Principal principal){
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in first.");
        }
		String avatarPath = payload.get("avatarPath");
		String email = principal.getName();
		userService.updateUserAvatar(email, avatarPath);
		return ResponseEntity.ok("Avatar updated");
	
}
}
