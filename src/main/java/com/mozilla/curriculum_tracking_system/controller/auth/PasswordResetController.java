package com.mozilla.curriculum_tracking_system.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mozilla.curriculum_tracking_system.dto.auth.PasswordResetRequest;
import com.mozilla.curriculum_tracking_system.dto.auth.ResetPasswordRequest;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.auth.IPasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final IPasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.initiatePasswordReset(request);
        return ResponseEntity.ok(new ApiResponse(
                "If the email exists in our system, a password reset link has been sent to your email address",
                null));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);

        if (isValid) {
            return ResponseEntity.ok(new ApiResponse("Reset token is valid", true));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse("Invalid or expired reset token", false));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse(
                "Password has been reset successfully. You can now login with your new password",
                null));
    }

}