package com.daniella.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.daniella.entity.Subscriber;
import com.daniella.repository.SubscriberRepository;

@Service
public class SubscriptionService {

	@Autowired
	private SubscriberRepository repository;

	@Autowired
	private JavaMailSender mailSender;

	public String subscribe(String email) {

		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email cannot be empty.");
		}

		email = email.trim().toLowerCase();

		if (repository.existsByEmail(email)) {
			return "You have already subscribed!";
		}

		Subscriber sub = new Subscriber();
		sub.setEmail(email);
		repository.save(sub);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(email);
			message.setSubject("Subscription Successful");
			message.setText("Thank you for subscribing!\n\n"
					+ "You will now receive updates about our Question Bank Application.\n\n" + "Stay tuned");

			mailSender.send(message);

		} catch (Exception e) {
			System.out.println("Email sending failed: " + e.getMessage());
		}

		return "Subscribed successfully!";
	}
}