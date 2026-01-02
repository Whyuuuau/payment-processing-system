package com.payment.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Audit logging interceptor for compliance
 * Logs all API requests and responses for payment operations
 */
@Slf4j
@Component
public class AuditLogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Log request details
        logRequest(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // Log response details
        logResponse(request, response, ex);
    }

    private void logRequest(HttpServletRequest request) {
        if (!shouldLog(request.getRequestURI())) {
            return;
        }

        String logMessage = String.format(
            "[AUDIT] Request: %s %s | IP: %s | User-Agent: %s | Timestamp: %s",
            request.getMethod(),
            request.getRequestURI(),
            getClientIp(request),
            request.getHeader("User-Agent"),
            LocalDateTime.now()
        );

        log.info(logMessage);

        // Log idempotency key if present (important for payment audit)
        String idempotencyKey = request.getHeader("X-Idempotency-Key");
        if (idempotencyKey != null) {
            log.info("[AUDIT] Idempotency-Key: {}", idempotencyKey);
        }
    }

    private void logResponse(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        if (!shouldLog(request.getRequestURI())) {
            return;
        }

        String logMessage = String.format(
            "[AUDIT] Response: %s %s | Status: %d | Duration: %dms",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            calculateDuration(request)
        );

        log.info(logMessage);

        // Log errors
        if (ex != null) {
            log.error("[AUDIT] Exception occurred: ", ex);
        }
    }

    private boolean shouldLog(String uri) {
        // Don't log health checks and actuator endpoints to reduce noise
        return !uri.contains("/health") && 
               !uri.contains("/actuator") && 
               !uri.contains("/swagger") &&
               !uri.contains("/api-docs");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private long calculateDuration(HttpServletRequest request) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime == null) {
            request.setAttribute("startTime", System.currentTimeMillis());
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }
}
