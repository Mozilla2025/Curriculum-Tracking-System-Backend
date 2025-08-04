package com.mozilla.curriculum_tracking_system.aspect;

import com.mozilla.curriculum_tracking_system.annotation.AdminOnly;
import com.mozilla.curriculum_tracking_system.constants.RoleConstants;
import com.mozilla.curriculum_tracking_system.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.file.AccessDeniedException;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminOnlyAspect {
    private final JwtUtil jwtUtil;

    public Object checkAdminAccess(ProceedingJoinPoint joinPoint, AdminOnly adminOnly) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AccessDeniedException("Unable to access request context");
        }

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("No Valid token provided");

        }

        String token = authHeader.substring(7);

        if (!jwtUtil.hasRole(token, RoleConstants.ADMIN)) {
            throw new AccessDeniedException(adminOnly.message());
        }
        return joinPoint.proceed();
    }
}
