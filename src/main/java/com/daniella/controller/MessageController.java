package com.daniella.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daniella.entity.Message;
import com.daniella.repository.MessageRepository;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

	private final MessageRepository messageRepository;

	public MessageController(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	@PostMapping("/submit")
	public String submitMessage(@RequestParam Long userId, @RequestParam String email, @RequestParam String message) {

		Message msg = new Message();
		msg.setUserId(userId);
		msg.setEmail(email);
		msg.setContent(message);
		msg.setCreatedAt(LocalDateTime.now());

		messageRepository.save(msg);

		return "Message sent successfully!";
	}

	@GetMapping("/all")
	public List<Message> getMessages() {
		return messageRepository.findAllByOrderByCreatedAtDesc();
	}
}
