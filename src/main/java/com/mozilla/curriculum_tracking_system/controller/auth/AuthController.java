package com.mozilla.curriculum_tracking_system.controller.auth;

import com.mozilla.curriculum_tracking_system.dto.auth.LoginRequest;
import com.mozilla.curriculum_tracking_system.dto.auth.LoginResponse;
import com.mozilla.curriculum_tracking_system.dto.auth.RefreshTokenRequest;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.auth.IAuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(new ApiResponse("Successfully logged in", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse("Successfully refreshed token", response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", authenticationService.getUserIdFromToken(token));
        profile.put("username", authenticationService.getUsernameFromToken(token));
        profile.put("email", authenticationService.getEmailFromToken(token));
        profile.put("firstName", authenticationService.getFirstNameFromToken(token));
        profile.put("lastName", authenticationService.getLastNameFromToken(token));
        profile.put("roles", authenticationService.getRolesFromToken(token));
        profile.put("permissions", buildPermissions(token));

        return ResponseEntity.ok(new ApiResponse("Successfully retrieved profile", profile));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse> validateToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        boolean isValid = authenticationService.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (isValid) {
            response.put("username", authenticationService.getUsernameFromToken(token));
            response.put("userId", authenticationService.getUserIdFromToken(token));
            response.put("email", authenticationService.getEmailFromToken(token));
            response.put("roles", authenticationService.getRolesFromToken(token));
            response.put("permissions", buildPermissions(token));
        } else {
            response.put("message", "Invalid or expired token");
        }

        return ResponseEntity.ok(new ApiResponse("Success", response));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse> getUserRoles(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        Map<String, Object> roleInfo = new HashMap<>();
        roleInfo.put("roles", authenticationService.getRolesFromToken(token));
        roleInfo.put("permissions", buildPermissions(token));

        return ResponseEntity.ok(new ApiResponse("Success", roleInfo));
    }

    @PostMapping("/check-role")
    public ResponseEntity<ApiResponse> checkRole(
            HttpServletRequest request,
            @RequestParam String roleName) {
        String token = extractTokenFromRequest(request);
        boolean hasRole = authenticationService.hasRole(token, roleName);

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasRole", hasRole);

        return ResponseEntity.ok(new ApiResponse("Success", response));
    }

    @PostMapping("/check-any-role")
    public ResponseEntity<ApiResponse> checkAnyRole(
            HttpServletRequest request,
            @RequestParam String[] roleNames) {
        String token = extractTokenFromRequest(request);
        boolean hasAnyRole = authenticationService.hasAnyRole(token, roleNames);

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasAnyRole", hasAnyRole);

        return ResponseEntity.ok(new ApiResponse("Success", response));
    }

    /**
     * Get basic user information (subset of profile)
     */
    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse> getUserInfo(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", authenticationService.getUserIdFromToken(token));
        userInfo.put("username", authenticationService.getUsernameFromToken(token));
        userInfo.put("email", authenticationService.getEmailFromToken(token));
        userInfo.put("firstName", authenticationService.getFirstNameFromToken(token));
        userInfo.put("lastName", authenticationService.getLastNameFromToken(token));

        return ResponseEntity.ok(new ApiResponse("Success", userInfo));
    }

    /**
     * Get user permissions based on roles
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse> getUserPermissions(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        Map<String, Object> response = new HashMap<>();
        response.put("permissions", buildPermissions(token));

        return ResponseEntity.ok(new ApiResponse("Success", response));
    }

    /**
     * Build permissions map based on user roles
     */
    private Map<String, Boolean> buildPermissions(String token) {
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("isAdmin", authenticationService.isAdmin(token));
        permissions.put("isDean", authenticationService.isDean(token));
        permissions.put("isViceChancellor", authenticationService.isViceChancellor(token));

        permissions.put("canManageUsers", authenticationService.isAdmin(token));

        return permissions;
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("No valid token found in request");
    }
}