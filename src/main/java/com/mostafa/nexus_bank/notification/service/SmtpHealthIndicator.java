package com.mostafa.nexus_bank.notification.service;

import jakarta.mail.Transport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmtpHealthIndicator implements ApplicationRunner {

    private final JavaMailSenderImpl mailSender;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    public SmtpHealthIndicator(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Verifying SMTP connection to {}:{}...", host, port);
        try {
            Transport transport = mailSender.getSession().getTransport("smtp");
            transport.connect(host, port, username, password);
            transport.close();
            log.info("SMTP connection to {}:{} verified successfully for user: {}", host, port, username);
        } catch (Exception e) {
            log.error("SMTP connection verification FAILED for {}:{} - {}", host, port, e.getMessage());
            log.error("Emails will NOT be sent. Check: 1) SMTP host/port, 2) username/password, 3) network/firewall.");
        }
    }
}
