package com.mozilla.curriculum_tracking_system.util;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mozilla.curriculum_tracking_system.exception.CustomException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtHelperService {

    private final JwtUtil jwtUtil;

    /**
     * Extract JWT token from Authorization header
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } throw new CustomException("Authorization token is required");
    }

    /**
     * Get current user ID from JWT token in servlet request
     */
    public Long getCurrentUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return getCurrentUserIdFromToken(token);
    }

    /**
     * Get current user ID from JWT token
     */
    public Long getCurrentUserIdFromToken(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new CustomException("Invalid token or expired");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new CustomException("User ID not found in token");
            }
            return userId;
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new CustomException("Failed to extract user information from token");
        }
    }

    /**
     * Get current username from JWT token in request
     */
    public String getCurrentUsernameFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return jwtUtil.getUsernameFromToken(token);
    }

    /**
     * Get current user email from JWT token in request
     */
    public String getCurrentUserEmailFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return jwtUtil.getEmailFromToken(token);
    }

    /**
     * Validate that the token belongs to the specified user ID
     * This can be used for additional security checks
     */
    public boolean validateTokenOwnership(String token, Long expectedUserId) {
        try {
            Long tokenUserId = getCurrentUserIdFromToken(token);
            return tokenUserId.equals(expectedUserId);
        } catch (Exception e) {
            return false;
        }
    }
}
