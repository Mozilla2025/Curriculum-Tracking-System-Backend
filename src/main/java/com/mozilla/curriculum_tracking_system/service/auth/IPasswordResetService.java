package com.mozilla.curriculum_tracking_system.service.auth;

import com.mozilla.curriculum_tracking_system.dto.auth.PasswordResetRequest;
import com.mozilla.curriculum_tracking_system.dto.auth.ResetPasswordRequest;

public interface IPasswordResetService {

    /**
     * Initiates password reset process by sending reset email
     * @param request containing user email
     */
    void initiatePasswordReset(PasswordResetRequest request);

    /**
     * Validates if a reset token is valid and not expired
     * @param token the reset token to validate
     * @return true if token is valid, false otherwise
     */
    boolean validateResetToken(String token);

    /**
     * Resets user password using valid token
     * @param request containing token and new password
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Cleanup expired tokens (for scheduled tasks)
     */
    void cleanupExpiredTokens();
}
