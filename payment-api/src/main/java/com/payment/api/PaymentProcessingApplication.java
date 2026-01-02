package com.payment.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Payment Processing System - Main Application
 * Enterprise-grade payment processing with resilience patterns
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.payment.api",
    "com.payment.core",
    "com.payment.infrastructure"
})
@EntityScan(basePackages = "com.payment.persistence.entity")
@EnableJpaRepositories(basePackages = "com.payment.persistence.repository")
public class PaymentProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessingApplication.class, args);
    }
}
