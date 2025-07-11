package com.mozilla.curriculum_tracking_system.service.auth;

import com.mozilla.curriculum_tracking_system.dto.auth.PasswordResetRequest;
import com.mozilla.curriculum_tracking_system.dto.auth.ResetPasswordRequest;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.model.user.PasswordResetToken;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.user.PasswordResetTokenRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService implements IPasswordResetService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    @Value("${app.password-reset.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Override
    @Transactional
    public void initiatePasswordReset(PasswordResetRequest request) {
        validateForgotPasswordRequest(request);

        try {
            Optional<User> userOptional = userRepository.findUserByEmailWithoutRoles(request.getEmail());

            if (userOptional.isEmpty()) {
                return;
            }

            User user = userOptional.get();

            if (!user.isEnabled()) {
                return;
            }

            if (passwordResetTokenRepository.existsByUserAndUsedFalseAndExpiryDateAfter(user, LocalDateTime.now())) {
                throw new BadRequestException(
                        "A password reset request is already pending. Please check your email or wait before requesting again.");
            }

            passwordResetTokenRepository.deleteByUser(user);
            passwordResetTokenRepository.flush();

            String resetToken = generateSecureToken();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenExpiryHours);

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .token(resetToken)
                    .user(user)
                    .expiryDate(expiryDate)
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(passwordResetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to process password reset request");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {

            Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository
                    .findValidTokenByToken(token, LocalDateTime.now());

            return tokenOptional.isPresent() && tokenOptional.get().isValid();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        validateResetPasswordRequest(request);

        try {
            PasswordResetToken resetToken = passwordResetTokenRepository
                    .findValidTokenByToken(request.getToken(), LocalDateTime.now())
                    .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

            if (!resetToken.isValid()) {
                throw new BadRequestException("Reset token has expired or has already been used");
            }

            User user = resetToken.getUser();

            if (!user.isEnabled()) {
                throw new BadRequestException("Account is disabled");
            }

            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            resetToken.setUsed(true);

            userRepository.save(user);
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetSuccessEmail(user.getEmail(), user.getUsername());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to reset password");
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        } catch (Exception e) {
        }
    }

    private void validateForgotPasswordRequest(PasswordResetRequest request) {
        if (request == null) {
            throw new BadRequestException("Password reset request is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
    }

    private void validateResetPasswordRequest(ResetPasswordRequest request) {
        if (request == null) {
            throw new BadRequestException("Reset password request is required");
        }
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            throw new BadRequestException("Reset token is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new BadRequestException("New password is required");
        }
        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long");
        }
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

}
