package com.mozilla.curriculum_tracking_system.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    private Set<String> roles;
}
