package com.daniella.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daniella.entity.Feedback;
import com.daniella.repository.FeedbackRepository;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

	@Autowired
	private FeedbackRepository feedbackRepository;

	// Save feedback (from student)
	@PostMapping("/submit")
	public String submitFeedback(@RequestParam String email, @RequestParam String message) {

		Feedback feedback = new Feedback(email, message);
		feedbackRepository.save(feedback);

		return "Thanks for reaching out. Your message has been received!";
	}

	// Admin fetch all messages
	@GetMapping("/all")
	public Object getAllFeedback() {
		return feedbackRepository.findAll();
	}
}