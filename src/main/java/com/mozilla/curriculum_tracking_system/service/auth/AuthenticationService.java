package com.mozilla.curriculum_tracking_system.service.auth;

import com.mozilla.curriculum_tracking_system.dto.auth.LoginRequest;
import com.mozilla.curriculum_tracking_system.dto.auth.LoginResponse;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.model.roles.Role;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        validateLoginRequest(request);

        try {
            Authentication authentication = authenticateUser(request);
            String username = authentication.getName();

            User user = userRepository.findActiveUserByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));


            validateUserAccount(user);

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getJwtRefreshExpirationMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(roles)
                    .build();

        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid username or password");
        } catch (DisabledException e) {
            throw new BadRequestException("Account is disabled");
        } catch (AuthenticationException e) {
            throw new BadRequestException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        try {
            if (!StringUtils.hasText(refreshToken)) {
                throw new BadRequestException("Refresh token is required");
            }

            if (!jwtUtil.validateToken(refreshToken)) {
                throw new BadRequestException("Invalid or expired refresh token");
            }

            String username = jwtUtil.getUsernameFromToken(refreshToken);
            User user = userRepository.findActiveUserByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            validateUserAccount(user);

            String newAccessToken = jwtUtil.generateAccessToken(user);

            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getJwtRefreshExpirationMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(roles)
                    .build();

        } catch (Exception e) {
            throw new BadRequestException("Failed to refresh token: " + e.getMessage());
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        Optional.ofNullable(request)
                .filter(r -> StringUtils.hasText(r.getUsername()))
                .filter(r -> StringUtils.hasText(r.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Username and password are required"));
    }

    private Authentication authenticateUser(LoginRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getUsernameFromToken(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Long getUserIdFromToken(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getEmailFromToken(String token) {
        try {
            return jwtUtil.getEmailFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getFirstNameFromToken(String token) {
        try {
            return jwtUtil.getFirstNameFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getLastNameFromToken(String token) {
        try {
            return jwtUtil.getLastNameFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<String> getRolesFromToken(String token) {
        try {
            return jwtUtil.getRolesFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean hasRole(String token, String roleName) {
        try {
            return jwtUtil.hasRole(token, roleName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean hasAnyRole(String token, String... roleNames) {
        try {
            return jwtUtil.hasAnyRole(token, roleNames);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean isAdmin(String token) {
        try {
            Boolean isAdmin = jwtUtil.isAdminFromToken(token);
            return Boolean.TRUE.equals(isAdmin);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean isQAAdmin(String token) {
        try {
            Boolean isQAAdmin = jwtUtil.isSeniorAdinFromToken(token);
            return Boolean.TRUE.equals(isQAAdmin);
        } catch (Exception e) {
            return false;
        }
    }



    private void validateUserAccount(User user) {
        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled");
        }

        if (!user.isAccountNonExpired()) {
            throw new BadRequestException("Account has expired");
        }

        if (!user.isAccountNonLocked()) {
            throw new BadRequestException("Account is locked");
        }

        if (!user.isCredentialsNonExpired()) {
            throw new BadRequestException("Credentials have expired");
        }
    }
}