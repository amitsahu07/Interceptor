package com.amitsahu07.Interceptor29.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component // Marks this class as a Spring component
@ConfigurationProperties(prefix = "spring.kafka.bootstrap-servers") // Binds properties starting with this prefix
public class KafkaPropertiesConfig {

    // This map will automatically be populated by Spring Boot.
    // For properties like:
    // spring.kafka.bootstrap-servers.cluster1=localhost:9092
    // spring.kafka.bootstrap-servers.cluster2=another-kafka-host:9093
    // 'clusters' will contain: { "cluster1": "localhost:9092", "cluster2": "another-kafka-host:9093" }
    private Map<String, String> clusters = new HashMap<>();

    public Map<String, String> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, String> clusters) {
        this.clusters = clusters;
    }
}