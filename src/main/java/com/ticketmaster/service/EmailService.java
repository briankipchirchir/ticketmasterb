package com.ticketmaster.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTicketEmail(String toEmail, String name, String tickets,
                                String eventName, Double amount) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("🎟️ Your Ticket is Confirmed – " + eventName);
        helper.setText("""
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
              <div style="background:#026cdf;padding:20px;border-radius:10px 10px 0 0;text-align:center;">
                <h1 style="color:white;margin:0;">🎟️ Ticket Confirmed!</h1>
              </div>
              <div style="background:#f9f9f9;padding:24px;border-radius:0 0 10px 10px;border:1px solid #ddd;">
                <p style="font-size:16px;">Hi <strong>%s</strong>,</p>
                <p>Your payment has been verified and your ticket is confirmed!</p>
                <div style="background:white;border:1px solid #ddd;border-radius:8px;padding:16px;margin:16px 0;">
                  <h3 style="color:#026cdf;margin-top:0;">Booking Details</h3>
                  <p><strong>Event:</strong> %s</p>
                  <p><strong>Tickets:</strong> %s</p>
                  <p><strong>Amount Paid:</strong> %s</p>
                </div>
                <p style="color:#555;font-size:13px;">Please show this email at the entrance. Enjoy the show! 🎉</p>
              </div>
            </div>
        """.formatted(name, eventName, tickets, amount), true);

        helper.setFrom("your-email@gmail.com"); // ← your sender email

        mailSender.send(message);
    }
}