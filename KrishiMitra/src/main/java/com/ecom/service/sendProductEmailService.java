package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ecom.model.ProductOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Service
public class sendProductEmailService {

	@Autowired
    private SendEmailService1 sendEmailService1;

	private void sendEmailToBuyer(ProductOrder order, String status) {
	    String buyerEmail = order.getUser().getEmail();

	    String subject = "Your Order Status Has Been Updated";
	    String body = "<html><body>"
	                + "<h3>Your order status has been updated!</h3>"
	                + "<p>Dear " + order.getUser().getName() + ",</p>"
	                + "<p>Your order with the ID: <b>" + order.getOrderId() + "</b> has been updated to the status: <b>" + status + "</b>.</p>"
	                + "<p>We will notify you if there are further updates.</p>"
	                + "<p>Thank you for shopping with us!</p>"
	                + "<p>Best regards,<br>Your Company Name</p>"
	                + "</body></html>";

	    try {
	        sendEmailService1.sendEmail(buyerEmail, subject, body);
	        System.out.println("Email sent successfully to: " + buyerEmail);
	    } catch (MessagingException e) {
	        e.printStackTrace();
	        // Log or handle the exception here
	        System.out.println("Error while sending email: " + e.getMessage());
	    }
	}
    }

