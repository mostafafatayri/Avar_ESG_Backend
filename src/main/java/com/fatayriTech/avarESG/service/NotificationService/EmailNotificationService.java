package com.fatayriTech.avarESG.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.notifications.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:no-reply@avaresg.com}")
    private String fromAddress;

    public void sendEmail(
            String recipient,
            String subject,
            String body
    ) {
        if (!emailEnabled) {
            log.info(
                    "Email notifications are disabled. Skipping email to {}",
                    recipient
            );
            return;
        }

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email is required"
            );
        }

        JavaMailSender mailSender =
                mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            throw new IllegalStateException(
                    "JavaMailSender is unavailable. " +
                            "Add spring-boot-starter-mail and configure SMTP."
            );
        }

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(
                subject == null || subject.isBlank()
                        ? "AVAR ESG Notification"
                        : subject
        );
        message.setText(
                body == null || body.isBlank()
                        ? "You have a new notification in AVAR ESG."
                        : body
        );

        mailSender.send(message);

        log.info(
                "Notification email sent to {}",
                recipient
        );
    }
}