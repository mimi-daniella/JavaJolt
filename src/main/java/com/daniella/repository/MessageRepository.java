package com.daniella.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daniella.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

	List<Message> findAllByOrderByCreatedAtDesc();
}