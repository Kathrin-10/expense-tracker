package com.expense.tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Test
    void sendsResetLinkToTheAccountEmail() {
        ObjectProvider<JavaMailSender> provider = mock();
        JavaMailSender sender = mock();
        when(provider.getIfAvailable()).thenReturn(sender);
        when(provider.getObject()).thenReturn(sender);

        EmailService service = new EmailService(provider, "smtp.example.com", "no-reply@example.com");
        service.sendPasswordReset("customer@example.com", "https://expenseflow.example/reset-password?token=abc");

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(messageCaptor.capture());
        assertEquals("customer@example.com", messageCaptor.getValue().getTo()[0]);
        assertTrue(messageCaptor.getValue().getText().contains("https://expenseflow.example/reset-password?token=abc"));
    }

    @Test
    void refusesToSendWhenSmtpIsNotConfigured() {
        ObjectProvider<JavaMailSender> provider = mock();
        EmailService service = new EmailService(provider, "", "no-reply@example.com");

        assertThrows(IllegalStateException.class,
                () -> service.sendPasswordReset("customer@example.com", "https://expenseflow.example/reset-password?token=abc"));
    }
}
