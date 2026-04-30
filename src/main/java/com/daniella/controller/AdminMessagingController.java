//package com.daniella.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//import com.daniella.repository.FeedbackRepository;
//
//@Controller
//public class AdminMessagingController {
//
//	private final FeedbackRepository feedbackRepository;
//
//	public AdminMessagingController(FeedbackRepository feedbackRepository) {
//		this.feedbackRepository = feedbackRepository;
//	}
//
//	// ✅ ONLY UNIQUE ROUTE (no conflicts)
//	@GetMapping("/admin/messages-view")
//	public String messagingPage(Model model) {
//
//		model.addAttribute("messages", feedbackRepository.findAll());
//
//		return "admin/messaging";
//	}
//}

package com.daniella.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.daniella.entity.Feedback;
import com.daniella.repository.FeedbackRepository;

@Controller
public class AdminMessagingController {

	private final FeedbackRepository feedbackRepository;

	public AdminMessagingController(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	@GetMapping("/admin/messages-view")
	public String messagingPage(Model model) {
		List<Feedback> messages = feedbackRepository.findAll().stream().sorted((a, b) -> {
			if (a.getCreatedAt() == null && b.getCreatedAt() == null)
				return 0;
			if (a.getCreatedAt() == null)
				return 1;
			if (b.getCreatedAt() == null)
				return -1;
			return b.getCreatedAt().compareTo(a.getCreatedAt());
		}).toList();

		model.addAttribute("messages", messages);
		return "admin/messaging";
	}
}
