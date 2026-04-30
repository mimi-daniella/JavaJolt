package com.daniella.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daniella.entity.Subscriber;
import com.daniella.repository.SubscriberRepository;

@Service
public class SubscriptionService {

	@Autowired
	private SubscriberRepository subscriberRepository;

	public String subscribe(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email cannot be empty.");
		}

		if (subscriberRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("This email is already subscribed.");
		}

		Subscriber subscriber = new Subscriber();
		subscriber.setEmail(email);
		subscriberRepository.save(subscriber);

		return "Successfully subscribed! Thank you for joining us.";
	}
}