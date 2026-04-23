//package com.daniella.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.daniella.service.SubscriptionService;
//
//@RestController
//@RequestMapping("/api")
//@CrossOrigin(origins = "*")
//public class SubscriptionController {
//
//	@Autowired
//	private SubscriptionService subscriptionService;
//
//	@PostMapping("/subscribe")
//	public ResponseEntity<String> subscribe(@RequestParam("email") String email) {
//
//		try {
//			String response = subscriptionService.subscribe(email);
//			return ResponseEntity.ok(response);
//
//		} catch (IllegalArgumentException e) {
//			return ResponseEntity.badRequest().body(e.getMessage());
//
//		} catch (Exception e) {
//			return ResponseEntity.internalServerError().body("Something went wrong. Please try again.");
//		}
//	}
//}

package com.daniella.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daniella.service.SubscriptionService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@PostMapping("/subscribe")
	public ResponseEntity<String> subscribe(@RequestParam("email") String email) {
		try {
			String response = subscriptionService.subscribe(email);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Something went wrong. Please try again.");
		}
	}
}