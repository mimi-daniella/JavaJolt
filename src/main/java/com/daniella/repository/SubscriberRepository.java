package com.daniella.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.Subscriber;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

	boolean existsByEmail(String email);
}
