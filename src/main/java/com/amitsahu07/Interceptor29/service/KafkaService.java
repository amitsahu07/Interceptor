package com.amitsahu07.Interceptor29.service;

import com.amitsahu07.Interceptor29.config.KafkaPropertiesConfig; // <--- NEW IMPORT

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
// REMOVE THIS IF IT'S STILL PRESENT: import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class KafkaService {

    // REMOVE THIS LINE:
    // @Value("#{${kafka.bootstrap.servers}}")
    // private Map<String, String> bootstrapServersConfig;

    private final Map<String, String> bootstrapServersConfig; // <--- THIS WILL NOW BE INJECTED

    // <--- ADD THIS CONSTRUCTOR
    public KafkaService(KafkaPropertiesConfig kafkaPropertiesConfig) {
        this.bootstrapServersConfig = kafkaPropertiesConfig.getClusters();
    }
    // ADD THIS CONSTRUCTOR --->

    // Method to get a list of configured cluster IDs
    public List<String> getClusterIds() {
        return new ArrayList<>(bootstrapServersConfig.keySet());
    }

    // Method to validate if a cluster ID is configured
    public boolean isValidClusterId(String clusterId) {
        return bootstrapServersConfig.containsKey(clusterId);
    }

    private AdminClient createAdminClient(String clusterId) {
        String bootstrapServers = bootstrapServersConfig.get(clusterId);
        if (bootstrapServers == null) {
            throw new IllegalArgumentException("No bootstrap servers configured for clusterId: " + clusterId);
        }
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(props);
    }

    public Set<String> listTopics(String clusterId) {
        try (AdminClient adminClient = createAdminClient(clusterId)) {
            ListTopicsOptions options = new ListTopicsOptions().listInternal(true);
            return adminClient.listTopics(options).names().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to list topics for cluster " + clusterId + ": " + e.getMessage(), e);
        }
    }

    public List<String> consumeMessages(String clusterId, String topic, int limit) {
        String bootstrapServers = bootstrapServersConfig.get(clusterId);
        if (bootstrapServers == null) {
            throw new IllegalArgumentException("No bootstrap servers configured for clusterId: " + clusterId);
        }

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "spring-boot-interceptor-consumer-group-" + UUID.randomUUID()); // Unique group ID for each consumption
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from the beginning of the topic

        List<String> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // Assign to all partitions of the topic
            List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                    .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                    .collect(Collectors.toList());
            consumer.assign(partitions);

            // Seek to the beginning of each assigned partition
            consumer.seekToBeginning(partitions);

            int messagesFetched = 0;
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 seconds timeout for fetching messages

            while (messagesFetched < limit && (System.currentTimeMillis() - startTime) < timeout) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // Poll for 100ms
                for (ConsumerRecord<String, String> record : records) {
                    messages.add(record.value());
                    messagesFetched++;
                    if (messagesFetched >= limit) {
                        break;
                    }
                }
                if (records.isEmpty() && (System.currentTimeMillis() - startTime) > 2000) {
                    // If no records are found for a while, and enough time has passed,
                    // it suggests no more messages are currently available.
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error consuming messages from Kafka: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to consume messages: " + e.getMessage(), e);
        }
        return messages;
    }
}