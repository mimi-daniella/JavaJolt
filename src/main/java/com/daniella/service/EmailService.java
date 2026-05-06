package com.daniella.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String fromAddress;

    @Value("${brevo.logo.url}")
    private String logoUrl; 

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendOtpEmail(String to, String otp) {
        String url = "https://api.brevo.com/v3/smtp/email";

        String htmlContent = String.format("""
        <html>
        <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
          <div style="max-width: 500px; margin: auto; background: #ffffff; border-radius: 8px; padding: 30px; border: 1px solid #ddd;">
            <div style="text-align: center; margin-bottom: 25px;">
              <img src="%s" alt="JavaJolt Logo" style="height: 50px;">
            </div>
            
            <h2 style="color: #333; text-align: center;">Verify Your Account</h2>
            <p style="font-size: 15px; color: #666; text-align: center;">Use the code below to complete your login:</p>
            
            <div style="text-align: center; margin: 30px 0;">
              <div style="display: inline-block; 12px 25px;">
                <span style="font-family: monospace; font-size: 32px; font-weight: bold; color: #000000; letter-spacing: 4px;">%s</span>
              </div>
            </div>
            
            <p style="font-size: 14px; color: #777; text-align: center; line-height: 1.5;">
              This code expires in 10 minutes.<br>
              <b>If you didn’t request this, please ignore this email.</b>
            </p>
            
            <p style="font-size: 11px; color: #aaa; text-align: center; margin-top: 30px;">© 2026 JavaJolt Quiz App</p>
          </div>
        </body>
        </html>
        """, logoUrl, otp);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromAddress));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", "JavaJolt - Your Verification Code");
        body.put("htmlContent", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Brevo response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error sending email via Brevo: " + e.getMessage());
        }
    }
}
