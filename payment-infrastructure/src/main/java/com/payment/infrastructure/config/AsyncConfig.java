package com.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Async configuration using Java 21 Virtual Threads
 * Virtual threads provide massive concurrency with minimal memory overhead
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Task executor using virtual threads
     * Virtual threads are lightweight and can handle millions of concurrent tasks
     * Perfect for I/O-bound operations like payment processing
     */
    @Bean(name = "paymentTaskExecutor")
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Alternative executor for CPU-intensive tasks
     * Uses fixed thread pool with processor count
     */
    @Bean(name = "cpuTaskExecutor")
    public Executor cpuTaskExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(processors);
    }
}
