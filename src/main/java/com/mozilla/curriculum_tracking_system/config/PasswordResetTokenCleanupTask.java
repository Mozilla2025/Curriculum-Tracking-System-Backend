package com.mozilla.curriculum_tracking_system.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mozilla.curriculum_tracking_system.service.auth.IPasswordResetService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenCleanupTask {

    private final IPasswordResetService passwordResetService;

    /**
     * Cleanup expired password reset tokens
     */

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        try {
            passwordResetService.cleanupExpiredTokens();
        } catch (Exception e) {

        }
    }

}
