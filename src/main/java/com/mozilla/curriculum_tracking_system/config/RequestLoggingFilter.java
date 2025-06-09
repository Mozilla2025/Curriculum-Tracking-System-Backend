package com.mozilla.curriculum_tracking_system.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
@Order(1)
@Slf4j
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Log request details
        log.info("=== INCOMING REQUEST ===");
        log.info("Method: {}", httpRequest.getMethod());
        log.info("URL: {}", httpRequest.getRequestURL().toString());
        log.info("URI: {}", httpRequest.getRequestURI());
        log.info("Query String: {}", httpRequest.getQueryString());
        log.info("Context Path: {}", httpRequest.getContextPath());
        log.info("Servlet Path: {}", httpRequest.getServletPath());
        log.info("Path Info: {}", httpRequest.getPathInfo());

        // Log all headers
        log.info("=== REQUEST HEADERS ===");
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = httpRequest.getHeader(headerName);
            log.info("{}: {}", headerName, headerValue);
        }

        // Continue with the filter chain
        long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;

        // Log response details
        log.info("=== RESPONSE ===");
        log.info("Status: {}", httpResponse.getStatus());
        log.info("Duration: {}ms", duration);
        log.info("========================");
    }
}