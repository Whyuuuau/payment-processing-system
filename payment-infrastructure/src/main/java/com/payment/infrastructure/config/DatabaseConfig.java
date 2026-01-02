package com.payment.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration with HikariCP connection pooling
 * Optimized for high throughput and connection management
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * HikariCP DataSource with optimized settings
     * HikariCP is the fastest connection pool available
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        
        // Connection pool settings
        config.setMaximumPoolSize(50); // Maximum connections
        config.setMinimumIdle(10); // Minimum idle connections
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        
        // Leak detection (warns if connection not returned in 60 seconds)
        config.setLeakDetectionThreshold(60000);
        
        // Connection test query
        config.setConnectionTestQuery("SELECT 1");
        
        // Performance settings
        config.setAutoCommit(false);
        config.setPoolName("PaymentHikariPool");
        
        // Additional PostgreSQL optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(config);
    }

    /**
     * JPA Entity Manager Factory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.payment.persistence.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        
        return em;
    }

    /**
     * Hibernate properties for optimization
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "true");
        
        // Batch processing
        properties.setProperty("hibernate.jdbc.batch_size", "50");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        
        // Query optimization
        properties.setProperty("hibernate.query.in_clause_parameter_padding", "true");
        properties.setProperty("hibernate.query.fail_on_pagination_over_collection_fetch", "true");
        
        // Second level cache disabled (using Redis instead)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        
        // Statistics
        properties.setProperty("hibernate.generate_statistics", "true");
        
        return properties;
    }

    /**
     * Transaction Manager
     */
    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
