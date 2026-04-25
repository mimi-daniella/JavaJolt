package com.daniella.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.daniella.repository.FeedbackRepository;

@Controller
public class AdminMessagingController {

	private final FeedbackRepository feedbackRepository;

	public AdminMessagingController(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	// ✅ ONLY UNIQUE ROUTE (no conflicts)
	@GetMapping("/admin/messages-view")
	public String messagingPage(Model model) {

		model.addAttribute("messages", feedbackRepository.findAll());

		return "admin/messaging";
	}
}