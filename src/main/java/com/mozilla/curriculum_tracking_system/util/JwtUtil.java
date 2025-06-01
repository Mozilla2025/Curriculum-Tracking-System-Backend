package com.mozilla.curriculum_tracking_system.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtUtil {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] secretBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof com.mozilla.curriculum_tracking_system.model.user.User user) {
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());
            claims.put("firstName", user.getFirstName());
            claims.put("lastName", user.getLastName());

            List<String> roleNames = user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toList());
            claims.put("roles", roleNames);

            claims.put("isAdmin", roleNames.contains("ADMIN"));
            claims.put("isDean", roleNames.contains("DEAN"));
            claims.put("isViceChancellor", roleNames.contains("VICE_CHANCELLOR"));
        }

        return createToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, jwtRefreshExpirationMs);
    }

    private String createToken(Map<String, Object> claims, String subject, int expirationMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token, claims -> {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
            return null;
        });
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token, claims -> (String) claims.get("email"));
    }

    
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return getClaimsFromToken(token, claims -> (List<String>) claims.get("roles"));
    }

    public Boolean isAdminFromToken(String token) {
        return getClaimsFromToken(token, claims -> (Boolean) claims.get("isAdmin"));
    }

    public Boolean isDeanFromToken(String token) {
        return getClaimsFromToken(token, claims -> (Boolean) claims.get("isDean"));
    }

    public Boolean isViceChancellorFromToken(String token) {
        return getClaimsFromToken(token, claims -> (Boolean) claims.get("isViceChancellor"));
    }

    public String getFirstNameFromToken(String token) {
        return getClaimsFromToken(token, claims -> (String) claims.get("firstName"));
    }

    public String getLastNameFromToken(String token) {
        return getClaimsFromToken(token, claims -> (String) claims.get("lastName"));
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateUserToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public Boolean hasRole(String token, String roleName) {
        try {
            List<String> roles = getRolesFromToken(token);
            return roles != null && roles.contains(roleName);
        } catch (Exception e) {
            log.error("Error checking role from token: {}", e.getMessage());
            return false;
        }
    }

    public Boolean hasAnyRole(String token, String... roleNames) {
        try {
            List<String> userRoles = getRolesFromToken(token);
            if (userRoles == null) return false;
            
            for (String roleName : roleNames) {
                if (userRoles.contains(roleName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking roles from token: {}", e.getMessage());
            return false;
        }
    }

    public Long getJwtRefreshExpirationMs() {
        return (long) jwtRefreshExpirationMs;
    }
}