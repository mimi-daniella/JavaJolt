package com.daniella.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.daniella.entity.Message;
import com.daniella.repository.MessageRepository;

@Service
public class MessageService {

	private final MessageRepository messageRepository;

	public MessageService(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	public void saveMessage(Message message) {
		messageRepository.save(message);
	}

	public List<Message> getAllMessages() {
		return messageRepository.findAllByOrderByCreatedAtDesc();
	}
}