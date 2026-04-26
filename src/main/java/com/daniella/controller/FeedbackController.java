package com.daniella.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daniella.entity.Feedback;
import com.daniella.repository.FeedbackRepository;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

	private final FeedbackRepository feedbackRepository;

	public FeedbackController(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	// ✅ FIXED: correct endpoint + proper form binding
	@PostMapping("/submit")
	public String submitFeedback(@ModelAttribute Feedback feedback) {
        if (feedback.getEmail() == null || feedback.getEmail().isBlank()
                || feedback.getMessage() == null || feedback.getMessage().isBlank()) {
            return "Email and message are required.";
        }

		feedback.setCreatedAt(LocalDateTime.now());

		feedbackRepository.save(feedback);

		return "Message sent successfully!";
	}

	@GetMapping("/all")
	public List<Feedback> getAllFeedback() {
		return feedbackRepository.findAll().stream().sorted((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
                return 0;
            }
            if (a.getCreatedAt() == null) {
                return 1;
            }
            if (b.getCreatedAt() == null) {
                return -1;
            }
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        }).toList();
	}
}
