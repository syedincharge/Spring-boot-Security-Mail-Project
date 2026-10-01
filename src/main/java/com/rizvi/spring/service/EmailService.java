package com.rizvi.spring.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    // ============================================================
    // Welcome email — plain text (kept simple)
    // ============================================================
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Authify, " + name + "!");
            message.setText(
                    "Hi " + name + ",\n\n" +
                            "Thanks for registering with Authify. We're glad to have you on board.\n\n" +
                            "If you have any questions, just reply to this email.\n\n" +
                            "Regards,\n" +
                            "The Authify Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send welcome email to " + toEmail, e);
        }
    }

    // ============================================================
    // Verify email — HTML with OTP
    // ============================================================
    public void sendVerifyOtpEmail(String toEmail, String name, String otp) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("otp", otp);
        ctx.setVariable("expiryMinutes", 15);

        String html = templateEngine.process("email/verify-email", ctx);

        sendHtmlEmail(toEmail, "Verify your Authify account", html);
    }

    // ============================================================
    // Reset password — HTML with OTP
    // ============================================================
    public void sendResetOtpEmail(String toEmail, String name, String otp) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("otp", otp);
        ctx.setVariable("expiryMinutes", 15);

        String html = templateEngine.process("email/password-reset-email", ctx);

        sendHtmlEmail(toEmail, "Reset your Authify password", html);
    }

    // ============================================================
    // Helper — sends HTML via MimeMessage
    // ============================================================
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);   // ⬅ true = HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}