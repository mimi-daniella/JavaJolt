package com.daniella.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/public")
public class PublicController{
	
	@GetMapping("/about")
	public String about() {
		return "/public/about";
	}
	
	@GetMapping("/contact")
	public String contact() {
		return "/public/contact";
	}

	// Simple public feedback handler added so the contact page no longer posts to a dead route.
	@PostMapping("/contact")
	public String submitContact(@RequestParam String email,
	                            @RequestParam String message,
	                            RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("success", "Thanks for reaching out. Your message has been received from " + email + ".");
		redirectAttributes.addFlashAttribute("preview", message);
		return "redirect:/public/contact";
	}
	
}
