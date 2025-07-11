package com.mozilla.curriculum_tracking_system.service.auth;

import com.mozilla.curriculum_tracking_system.dto.auth.LoginRequest;
import com.mozilla.curriculum_tracking_system.dto.auth.LoginResponse;

import java.util.List;

public interface IAuthenticationService {

    // Authentication methods
    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);

    boolean validateToken(String token);

    // Token extraction methods
    String getUsernameFromToken(String token);

    Long getUserIdFromToken(String token);

    String getEmailFromToken(String token);

    String getFirstNameFromToken(String token);

    String getLastNameFromToken(String token);

    List<String> getRolesFromToken(String token);

    // Role checking methods
    boolean hasRole(String token, String roleName);

    boolean hasAnyRole(String token, String... roleNames);

    boolean isAdmin(String token);

    boolean isQAAdmin(String token);

}