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

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());

            helper.setReplyTo(fromEmail);

            message.setHeader("X-Priority", "3");
            message.setHeader("X-MSMail-Priority", "Normal");

            message.setHeader("X-Mailer", "Curriculum Tracking System");
            message.setHeader("X-Auto-Response-Suppress", "OOF, AutoReply");

            String content;
            if (emailRequest.getTemplateName() != null && !emailRequest.getTemplateName().isEmpty()) {
                content = processTemplate(emailRequest.getTemplateName(), emailRequest.getVariables());

            } else {
                content = "Default email content";
            }

            if (emailRequest.isHtml()) {
                helper.setText(generatePlainTextVersion(content), content);
            } else {
                helper.setText(content, false);
            }

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
        variables.put("loginUrl", frontendUrl + "/login");
        variables.put("supportEmail", fromEmail);
        variables.put("companyName", "Mozilla Foundation");
        variables.put("currentYear", java.time.Year.now().getValue());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(credentialsData.getEmail())
                .subject("🎓 Welcome! Your Account Credentials - Curriculum Tracking System")
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
        variables.put("resetLink", frontendUrl + "/reset-password?token=" + resetToken);
        variables.put("supportEmail", fromEmail);
        variables.put("companyName", "Mozilla Foundation");
        variables.put("currentYear", java.time.Year.now().getValue());
        variables.put("expiryTime", "24 hours");

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("🔐 Password Reset Request - Curriculum Tracking System")
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
        variables.put("loginLink", frontendUrl + "/login");
        variables.put("supportEmail", fromEmail);
        variables.put("companyName", "Mozilla Foundation");
        variables.put("currentYear", java.time.Year.now().getValue());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("🎉 Welcome to Curriculum Tracking System")
                .templateName("welcome")
                .variables(variables)
                .isHtml(true)
                .build();

        sendEmail(emailRequest);
    }

    @Override
    public void sendPasswordResetSuccessEmail(String email, String username) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("loginUrl", frontendUrl + "/login");
        variables.put("loginLink", frontendUrl + "/login");
        variables.put("supportEmail", fromEmail);
        variables.put("companyName", "Mozilla Foundation");
        variables.put("currentYear", java.time.Year.now().getValue());
        variables.put("timestamp", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("✅ Password Reset Successful - Curriculum Tracking System")
                .templateName("password-reset-success")
                .variables(variables)
                .isHtml(true)
                .build();

        sendEmail(emailRequest);
    }

    private String processTemplate(String templateName, Map<String, Object> variables) {
        Context contex = new Context();
        if (variables != null) {
            contex.setVariables(variables);
        }

        return templateEngine.process("email/" + templateName, contex);
    }

    private String generatePlainTextVersion(String htmlContent) {
        return htmlContent
                .replaceAll("<[^>]+>", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

}



