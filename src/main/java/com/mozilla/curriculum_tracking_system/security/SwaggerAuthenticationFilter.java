package com.mozilla.curriculum_tracking_system.security;

import com.mozilla.curriculum_tracking_system.controller.docs.SwaggerAuthController;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Filter to protect Swagger documentation endpoints with password authentication
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "app.docs.password-protection.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class SwaggerAuthenticationFilter implements Filter {

    private static final String[] PROTECTED_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/",
            "/api-docs",
            "/v3/api-docs"
    };

    private static final String[] EXCLUDED_PATHS = {
            "/docs/auth",
            "/docs/authenticate",
            "/docs/logout"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();

        if (isExcludedPath(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        if (isProtectedDocumentationPath(requestPath)) {
            HttpSession session = httpRequest.getSession();

            if (!SwaggerAuthController.isAuthenticated(session)) {
                log.debug("Unauthenticated access attempt to documentation: {} from IP: {}",
                        requestPath, getClientIpAddress(httpRequest));

                String redirectUrl = UriComponentsBuilder.fromPath("/docs/auth")
                        .queryParam("redirectUrl", URLEncoder.encode(requestPath, StandardCharsets.UTF_8))
                        .build()
                        .toUriString();

                httpResponse.sendRedirect(redirectUrl);
                return;
            }

            log.debug("Authenticated access to documentation: {} from IP: {}",
                    requestPath, getClientIpAddress(httpRequest));
        }

        chain.doFilter(request, response);
    }

    /**
     * Check if the request path is a protected documentation endpoint
     */
    private boolean isProtectedDocumentationPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }

        for (String protectedPath : PROTECTED_PATHS) {
            if (requestPath.equals(protectedPath) || requestPath.startsWith(protectedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the request path should be excluded from authentication
     */
    private boolean isExcludedPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }

        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestPath.equals(excludedPath) || requestPath.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
