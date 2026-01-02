package com.payment.api.config;

import com.payment.api.interceptor.AuditLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration
 * Registers interceptors and other web configurations
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditLogInterceptor auditLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add audit logging for all payment API endpoints
        registry.addInterceptor(auditLogInterceptor)
                .addPathPatterns("/api/v1/payments/**");
    }
}
