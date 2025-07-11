package com.mozilla.curriculum_tracking_system.aspect;

import java.nio.file.AccessDeniedException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mozilla.curriculum_tracking_system.annotation.AdminOnly;
import com.mozilla.curriculum_tracking_system.constants.RoleConstants;
import com.mozilla.curriculum_tracking_system.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminOnlyAspect {
    private final JwtUtil jwtUtil;

    @Around("@annotation(adminOnly)")
    public Object checkAdminAccess(ProceedingJoinPoint joinPoint, AdminOnly adminOnly) throws Throwable {
        String token = getRequestAttributes();

        if (jwtUtil.hasRole(token, RoleConstants.QA)) {
            log.debug("QA user accessing admin-only resource");
            return joinPoint.proceed();
        }

        if (jwtUtil.hasRole(token, RoleConstants.ADMIN)) {
            log.debug("Admin user accessing admin-only resource");
            return joinPoint.proceed();
        }

        log.warn("Non-admin user attempted to access admin-only resource");
        throw new AccessDeniedException(adminOnly.message());
    }

    private static String getRequestAttributes() throws AccessDeniedException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AccessDeniedException("Unable to access request context");
        }

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("No valid token provided");
        }

        return authHeader.substring(7);
    }
}