package com.daniella.admin;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.daniella.entity.User;
import com.daniella.repository.UserRepository; 
import com.daniella.service.UserService;


@Controller
public class UserManagement{
	
	private final UserRepository userRepository;
	private final UserService userService;
	public UserManagement(UserRepository userRepository, UserService userService ) {
		this.userRepository = userRepository;
		this.userService = userService;
	}

	@GetMapping("/admin/users")
	public String users(Model model) {
		model.addAttribute("activePage", "users");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName(); 
		
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found") );
		
		model.addAttribute("firstName", user.getFirstName());
		model.addAttribute("lastName", user.getLastName());
		model.addAttribute("email", user.getEmail());		
		model.addAttribute("totalUsers", userService.getTotalUsers());		
		
		return "admin/users";
		
	}

}
