package com.daniella.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@GetMapping("/messaging")
	public String messaging() {
		return "/admin/messaging";
	}
	
	@GetMapping("/settings")
	public String settings() {
		return "/admin/settings";
	}
}
