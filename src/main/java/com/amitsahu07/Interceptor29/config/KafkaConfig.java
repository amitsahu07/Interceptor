package com.amitsahu07.Interceptor29.config;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class KafkaConfig {

    private final Map<String, ConsumerFactory<String, String>> consumerFactories = new ConcurrentHashMap<>();
    @Value("${spring.kafka.bootstrap-servers.cluster1}")
    private String cluster1BootstrapServers;
    @Value("${spring.kafka.bootstrap-servers.cluster2}")
    private String cluster2BootstrapServers;

    @PostConstruct
    public void init() {
        consumerFactories.put("cluster1", new DefaultKafkaConsumerFactory<>(consumerProps(cluster1BootstrapServers)));
        consumerFactories.put("cluster2", new DefaultKafkaConsumerFactory<>(consumerProps(cluster2BootstrapServers)));
    }

    private Map<String, Object> consumerProps(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "interceptor-app-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    public ConsumerFactory<String, String> getConsumerFactory(String clusterId) {
        ConsumerFactory<String, String> factory = consumerFactories.get(clusterId);
        if (factory == null) {
            throw new IllegalArgumentException("Kafka cluster with ID '" + clusterId + "' is not configured.");
        }
        return factory;
    }

    // REMOVED: @Bean AdminClient adminClient() method from here
    // It will now be created on demand in KafkaService
}