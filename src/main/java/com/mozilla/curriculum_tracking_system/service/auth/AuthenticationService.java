package com.mozilla.curriculum_tracking_system.service.auth;

import com.mozilla.curriculum_tracking_system.dto.auth.LoginRequest;
import com.mozilla.curriculum_tracking_system.dto.auth.LoginResponse;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService implements IAuthenticationService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            User user = findUserByUsername(request.getUsername());
            
            validateUserAccount(user);
            
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
            
            
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getJwtRefreshExpirationMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirst_name())
                    .lastName(user.getLast_name())
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
    public LoginResponse refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new BadRequestException("Invalid or expired refresh token");
            }
            
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            User user = findUserByUsername(username);
            
            validateUserAccount(user);
            
            String newAccessToken = jwtUtil.generateAccessToken(user);
            
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
            
            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) 
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getJwtRefreshExpirationMs())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirst_name())
                    .lastName(user.getLast_name())
                    .roles(roles)
                    .build();
                    
        } catch (Exception e) {
            throw new BadRequestException("Failed to refresh token: " + e.getMessage());
        }
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
    public String getUsernameFromToken(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }
    
    @Override
    public Long getUserIdFromToken(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }
    
    @Override
    public String getEmailFromToken(String token) {
        try {
            return jwtUtil.getEmailFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }
    
    @Override
    public String getFirstNameFromToken(String token) {
        try {
            return jwtUtil.getFirstNameFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }
    
    @Override
    public String getLastNameFromToken(String token) {
        try {
            return jwtUtil.getLastNameFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }
    
    @Override
    public List<String> getRolesFromToken(String token) {
        try {
            return jwtUtil.getRolesFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Invalid token");
        }
    }
    
    @Override
    public boolean hasRole(String token, String roleName) {
        try {
            return jwtUtil.hasRole(token, roleName);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean hasAnyRole(String token, String... roleNames) {
        try {
            return jwtUtil.hasAnyRole(token, roleNames);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isAdmin(String token) {
        try {
            Boolean isAdmin = jwtUtil.isAdminFromToken(token);
            return isAdmin != null && isAdmin;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isDean(String token) {
        try {
            Boolean isDean = jwtUtil.isDeanFromToken(token);
            return isDean != null && isDean;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isViceChancellor(String token) {
        try {
            Boolean isViceChancellor = jwtUtil.isViceChancellorFromToken(token);
            return isViceChancellor != null && isViceChancellor;
        } catch (Exception e) {
            return false;
        }
    }
    
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
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