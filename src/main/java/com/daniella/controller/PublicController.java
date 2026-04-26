package com.daniella.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.daniella.entity.Feedback;
import com.daniella.repository.FeedbackRepository;

@Controller
@RequestMapping("/public")
public class PublicController{

	private final FeedbackRepository feedbackRepository;

	public PublicController(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}
	
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
		Feedback feedback = new Feedback();
		feedback.setEmail(email);
		feedback.setMessage(message);
		feedback.setCreatedAt(LocalDateTime.now());
		feedbackRepository.save(feedback);

		redirectAttributes.addFlashAttribute("success", "Thanks for reaching out. Your message has been received.");
		return "redirect:/public/contact";
	}
	
}
