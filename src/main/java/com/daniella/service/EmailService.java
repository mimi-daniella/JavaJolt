package com.daniella.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject("JavaJolt - Your Verification Code");

        String htmlContent = String.format("""
        <html>
        <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
          <div style="max-width: 500px; margin: auto; background: #ffffff; border-radius: 8px; padding: 30px; border: 1px solid #ddd;">
            <div style="text-align: center; margin-bottom: 25px;">
              <img src="cid:logoImage" alt="JavaJolt Logo" style="height: 50px;">
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
        """, otp);

        //text adding inline resources
        helper.setText(htmlContent, true);

        ClassPathResource logo = new ClassPathResource("static/images/fullLogo.png");
        helper.addInline("logoImage", logo);

        mailSender.send(mimeMessage);
    }
}
