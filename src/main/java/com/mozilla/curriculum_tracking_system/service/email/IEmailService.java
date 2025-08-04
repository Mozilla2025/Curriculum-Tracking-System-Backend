package com.mozilla.curriculum_tracking_system.service.email;

import com.mozilla.curriculum_tracking_system.dto.email.EmailRequest;
import com.mozilla.curriculum_tracking_system.dto.email.UserCredentialsEmailData;
import com.mozilla.curriculum_tracking_system.dto.notification.NotificationDto;

public interface IEmailService {
    void sendEmail(EmailRequest emailRequest);

    void sendUserCredentialsEmail(UserCredentialsEmailData credentialsEmailData);

    void sendPasswordResetEmail(String email, String resetToken);

    void sendWelcomeEmail(String email, String username);

    void sendPasswordResetSuccessEmail(String email, String username);

    //Notification-related methods

    void sendCurriculumDueForReviewNorification(String recipientEmail, String recipientUser);

    void sendNotificationEmail(NotificationDto notificationDto);

}
