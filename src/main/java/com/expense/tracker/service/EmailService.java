package com.expense.tracker.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final ObjectProvider<JavaMailSender> mailSenderProvider;
  private final String mailHost;
  private final String fromAddress;

  public EmailService(
      ObjectProvider<JavaMailSender> mailSenderProvider,
      @Value("${spring.mail.host:}") String mailHost,
      @Value("${app.mail.from:}") String fromAddress) {
    this.mailSenderProvider = mailSenderProvider;
    this.mailHost = mailHost;
    this.fromAddress = fromAddress;
  }

  public void ensureConfigured() {
    if (mailHost.isBlank()
        || fromAddress.isBlank()
        || mailSenderProvider.getIfAvailable() == null) {
      throw new IllegalStateException(
          "Password reset email is not configured. Set MAIL_HOST, MAIL_FROM, and the SMTP"
              + " credentials in your environment.");
    }
  }

  public void sendPasswordReset(String recipient, String resetUrl) {
    ensureConfigured();
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipient);
    message.setSubject("Reset your ExpenseFlow password");
    message.setText(
        """
        We received a request to reset your ExpenseFlow password.

        Use the link below within 30 minutes. It can only be used once:
        %s

        If you did not request a password reset, you can ignore this email.
        """
            .formatted(resetUrl));
    mailSenderProvider.getObject().send(message);
  }
}
