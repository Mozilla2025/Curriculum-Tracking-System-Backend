package com.mozilla.curriculum_tracking_system.controller.docs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling Swagger documentation authentication
 */
@Controller
@RequestMapping("/docs")
@RequiredArgsConstructor
@Slf4j
public class SwaggerAuthController {

    private static final String DOCS_AUTH_SESSION_KEY = "docs_authenticated";
    private static final String DOCS_AUTH_TIMESTAMP_KEY = "docs_auth_timestamp";
    private static final long SESSION_TIMEOUT_MS = 24 * 60 * 60 * 1000;

    @Value("${app.docs.password:SwaggerDocs@2025}")
    private String docsPassword;

    @Value("${app.docs.session-timeout-hours:24}")
    private int sessionTimeoutHours;

    /**
     * Display the documentation authentication page
     */
    @GetMapping("/auth")
    public String showAuthPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();

        // Check if already authenticated and session is still valid
        if (isAuthenticated(session)) {
            return "redirect:/swagger-ui.html";
        }

        return "docs/auth";
    }

    /**
     * Process authentication attempt
     */
    @PostMapping("/authenticate")
    public String authenticate(
            @RequestParam("password") String password,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession();

        if (docsPassword.equals(password)) {
            // Authentication successful
            session.setAttribute(DOCS_AUTH_SESSION_KEY, true);
            session.setAttribute(DOCS_AUTH_TIMESTAMP_KEY, System.currentTimeMillis());

            log.info("Documentation access granted from IP: {}", getClientIpAddress(request));

            String targetUrl = redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/swagger-ui.html";
            return "redirect:" + targetUrl;
        } else {
            // Authentication failed
            log.warn("Failed documentation authentication attempt from IP: {}", getClientIpAddress(request));
            redirectAttributes.addFlashAttribute("error", "Invalid password. Please try again.");

            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                redirectAttributes.addAttribute("redirectUrl", redirectUrl);
            }

            return "redirect:/docs/auth";
        }
    }

    /**
     * Logout from documentation access
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(DOCS_AUTH_SESSION_KEY);
            session.removeAttribute(DOCS_AUTH_TIMESTAMP_KEY);
            log.info("Documentation access logged out from IP: {}", getClientIpAddress(request));
        }

        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully.");
        return "redirect:/docs/auth";
    }

    /**
     * Check if the current session is authenticated for documentation access
     */
    public static boolean isAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }

        Boolean authenticated = (Boolean) session.getAttribute(DOCS_AUTH_SESSION_KEY);
        Long timestamp = (Long) session.getAttribute(DOCS_AUTH_TIMESTAMP_KEY);

        if (authenticated == null || !authenticated || timestamp == null) {
            return false;
        }

        // Check if session has expired
        long currentTime = System.currentTimeMillis();
        if (currentTime - timestamp > SESSION_TIMEOUT_MS) {
            // Session expired, clear authentication
            session.removeAttribute(DOCS_AUTH_SESSION_KEY);
            session.removeAttribute(DOCS_AUTH_TIMESTAMP_KEY);
            return false;
        }

        return true;
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
