package com.amitsahu07.Interceptor29.controller;

import com.amitsahu07.Interceptor29.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    private final KafkaService kafkaService;

    @Autowired
    public KafkaController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    // Endpoint to get the list of configured Kafka clusters
    @GetMapping("/clusters")
    public ResponseEntity<List<String>> getKafkaClusters() {
        List<String> clusterIds = kafkaService.getClusterIds();
        return ResponseEntity.ok(clusterIds);
    }

    // Modified endpoint to fetch topics with optional changeNo and incident parameters
    @GetMapping("/topics")
    public ResponseEntity<Set<String>> getTopics(
            @RequestParam String clusterId,
            @RequestParam(required = false) String changeNo, // NEW PARAMETER
            @RequestParam(required = false) String incident   // NEW PARAMETER
    ) {
        if (!kafkaService.isValidClusterId(clusterId)) {
            return ResponseEntity.badRequest().body(Collections.emptySet()); // Or an error message
        }
        Set<String> topics = kafkaService.listTopics(clusterId);

        // You can add logic here to use changeNo or incident
        // For example, logging them or passing them to KafkaService if needed
        System.out.println("Fetching topics for cluster: " + clusterId +
                ", Change No: " + (changeNo != null ? changeNo : "N/A") +
                ", Incident: " + (incident != null ? incident : "N/A"));

        return ResponseEntity.ok(topics);
    }

    // Endpoint to get messages from a topic (existing)
    @GetMapping("/messages")
    public ResponseEntity<List<String>> getMessages(
            @RequestParam String clusterId,
            @RequestParam String topic,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (!kafkaService.isValidClusterId(clusterId)) {
            return ResponseEntity.badRequest().build();
        }
        List<String> messages = kafkaService.consumeMessages(clusterId, topic, limit);
        return ResponseEntity.ok(messages);
    }

    // Existing /userinfo endpoint
    @GetMapping("/userinfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return ResponseEntity.ok(Map.of(
                    "username", userDetails.getUsername(),
                    "roles", userDetails.getAuthorities().stream().map(Object::toString).toList()
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}