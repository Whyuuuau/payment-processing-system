package com.payment.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for event-driven architecture
 * Producer: idempotent with exactly-once semantics
 * Consumer: manual offset commit for reliability
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:payment-service-group}")
    private String consumerGroupId;

    /**
     * Producer configuration with idempotent delivery
     * Ensures exactly-once message delivery
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Enable idempotence to prevent duplicate messages
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Require all replicas to acknowledge (strongest durability)
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // Retries for transient failures
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        // Max in-flight requests (must be <= 5 for idempotence)
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // Enable compression for better network utilization
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Batching for better throughput
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        
        // Transactional producer for exactly-once semantics
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "payment-producer-tx-");
        
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer configuration with manual offset management
     * Provides control over message acknowledgment
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Manual offset commit for precise control
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Start from earliest if no offset exists
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Isolation level for reading transactional messages
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        // Trust all packages for JSON deserialization
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        // Max poll records
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Manual acknowledgment mode
        factory.getContainerProperties().setAckMode(
            org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );
        
        // Concurrency level (number of consumer threads)
        factory.setConcurrency(3);
        
        return factory;
    }
}
