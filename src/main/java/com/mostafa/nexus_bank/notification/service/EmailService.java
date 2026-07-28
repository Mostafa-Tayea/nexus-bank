package com.mostafa.nexus_bank.notification.service;

import com.mostafa.nexus_bank.exception.EmailSendingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final String defaultFromAddress;

    public EmailService(JavaMailSender javaMailSender,
                        @Value("${spring.mail.username}") String defaultFromAddress) {
        this.javaMailSender = javaMailSender;
        this.defaultFromAddress = defaultFromAddress;
    }

    public void sendEmail(String to, String subject, String htmlBody) {
        log.info("Attempting to send email to: {} with subject: {}", to, subject);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(defaultFromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            javaMailSender.send(message);
            log.info("Email sent successfully to: {} with subject: {}", to, subject);
        } catch (MailException e) {
            log.error("MailException sending email to: {} with subject: {}. Root cause: {}",
                    to, subject, e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage(), e);
            throw new EmailSendingException("Failed to send email to " + to + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {} with subject: {}", to, subject, e);
            throw new EmailSendingException("Failed to send email to " + to, e);
        }
    }
}
