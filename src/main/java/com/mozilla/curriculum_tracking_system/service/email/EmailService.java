package com.mozilla.curriculum_tracking_system.service.email;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.mozilla.curriculum_tracking_system.dto.email.EmailRequest;
import com.mozilla.curriculum_tracking_system.dto.email.UserCredentialsEmailData;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name:Curriculum Tracking System}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());

            String content;
            if (emailRequest.getTemplateName() != null && !emailRequest.getTemplateName().isEmpty()) {
                content = processTemplate(emailRequest.getTemplateName(), emailRequest.getVariables());

            } else {
                content = "Default email content";
            }
            helper.setText(content, emailRequest.isHtml());

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BadRequestException("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException("Failed to send email due to unexpected error");
        }
    }

    @Override
    public void sendUserCredentialsEmail(UserCredentialsEmailData credentialsData) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", credentialsData.getUsername());
        variables.put("email", credentialsData.getEmail());
        variables.put("password", credentialsData.getPassword());
        variables.put("firstName", credentialsData.getFirstName());
        variables.put("lastName", credentialsData.getLastName());
        variables.put("roleName", credentialsData.getRoleName());
        variables.put("loginUrl",
                credentialsData.getLoginUrl() != null ? credentialsData.getLoginUrl() : frontendUrl + "/login");
        variables.put("supportEmail", fromEmail);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(credentialsData.getEmail())
                .subject("Welcome! Your Account Credentials")
                .templateName("user-credentials")
                .variables(variables)
                .isHtml(true)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("resetUrl", frontendUrl + "/reset-password?token=" + resetToken);
        variables.put("supportEmail", fromEmail);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("Password Reset Request")
                .templateName("password-reset")
                .variables(variables)
                .isHtml(true)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    public void sendWelcomeEmail(String email, String username) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("loginUrl", frontendUrl + "/login");
        variables.put("supportEmail", fromEmail);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("Welcome to Curriculum Tracking System")
                .templateName("welcome")
                .variables(variables)
                .isHtml(true)
                .build();

        sendEmail(emailRequest);
    }

    private String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        return templateEngine.process("email/" + templateName, context);
    }

}
