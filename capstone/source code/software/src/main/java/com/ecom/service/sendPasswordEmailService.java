package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Service
public class sendPasswordEmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmailId;

    public void sendPasswordResetLink(String recipient, String resetUrl) throws MessagingException {
        String subject = "Password Reset Request";
        String body = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;display: flex; flex-direction:column; justify-content: center; align-items: center;	'>"
                + "<div style='width: 100%; height: 50vh; display: flex; flex-direction:column; justify-content: center; align-items: center; background-color: #f4f4f4;'>"
                + "<div style='background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); width: 90%; max-width: 600px;'>"
                + "<center>"
                + "<h3 style='color: #333; font-size: 24px; font-weight: bold;'>Reset your password</h3>"
                + "<p style='color: #555; font-size: 16px;'>Click the Button below to reset your password</p>"
                + "<a href=\"" + resetUrl + "\" style='text-decoration: none;'>"
                + "<button style='background-color: #4CAF50; color: white; padding: 15px 30px; font-size: 16px; border: none; border-radius: 5px; cursor: pointer;'>Reset Password</button>"
                + "</a>"
                + "<p style='color: #555; font-size: 14px; margin-top: 20px;'>If you did not request this change, please ignore this email.</p>"
                + "</center>"
                + "</div>"
                + "</div>"
                + "</body></html>";


        // Create MimeMessage for HTML content
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        
        // Use MimeMessageHelper to set up the email
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // 'true' indicates HTML email

        // Set email properties
        helper.setFrom(fromEmailId);
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(body, true);  // 'true' means the body is HTML

        // Send the email
        javaMailSender.send(mimeMessage);
    }
}
