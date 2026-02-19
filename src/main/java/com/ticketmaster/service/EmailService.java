package com.ticketmaster.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import org.springframework.stereotype.Service;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;


@Service
public class EmailService {

    public void sendTicketEmail(String toEmail, String name, String tickets,
                                String eventName, Double amount) throws Exception {

        Email from = new Email("ticketmasterresaleservices9@gmail.com");
        Email to = new Email(toEmail);
        String subject = "🎟️ Your Ticket is Confirmed – " + eventName;

        String html = """
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
        """.formatted(name, eventName, tickets, amount);

        Content content = new Content("text/html", html);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sg.api(request);
    }
}