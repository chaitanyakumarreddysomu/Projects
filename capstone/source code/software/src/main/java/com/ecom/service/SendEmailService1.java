package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

// this is for sending the product status

@Service
public class SendEmailService1 {

	@Autowired
    private JavaMailSender mailSender; // Automatically configured by Spring Boot

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        // Create a simple mail message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        
        // Send the message
        mailSender.send(message);
    }    }

