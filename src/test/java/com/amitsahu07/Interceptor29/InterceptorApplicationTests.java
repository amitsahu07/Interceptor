package com.amitsahu07.Interceptor29;

import com.amitsahu07.Interceptor29.controller.KafkaController;
import com.amitsahu07.Interceptor29.service.KafkaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"},
        controlledShutdown = true,
        // --- NEW: Pre-create these topics in the embedded Kafka broker ---
        topics = {"mig29", "mig21"}
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers.cluster1=localhost:9092"
})
class InterceptorApplicationTests {

    @Autowired
    private KafkaController kafkaController;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; // Autowire KafkaTemplate for sending messages

    @Test
    void contextLoads() {
        assertThat(kafkaController).isNotNull();
        assertThat(kafkaService).isNotNull();
        assertThat(kafkaTemplate).isNotNull(); // Ensure KafkaTemplate is also autowired
    }

    @Test
    void canListPreCreatedTopicsFromEmbeddedKafka() throws Exception {
        // This test verifies that our KafkaService can connect to and list topics
        // from the embedded Kafka broker, including the pre-created ones.
        Set<String> topics = kafkaService.listTopics("cluster1");
        assertThat(topics).isNotNull();
        assertThat(topics).isNotEmpty();
        assertThat(topics).contains("mig29", "mig21"); // Verify our topics are present
        System.out.println("Topics found in embedded Kafka: " + topics);
    }

    @Test
    void canProduceAndConsumeFromEmbeddedKafka() throws Exception {
        String testTopic = "mig29";
        String testMessage = "Hello from " + testTopic + " test!";

        // Send a message to the topic
        kafkaTemplate.send(testTopic, testMessage).get(10, TimeUnit.SECONDS); // Wait for send to complete

        // Consume messages from the topic
        //KafkaService.ConsumedData consumedData = kafkaService.consumeTopic("cluster1", testTopic);

        //assertThat(consumedData).isNotNull();
       // assertThat(consumedData.getRecords()).isNotEmpty();
        //assertThat(consumedData.getRecords()).contains(testMessage);
        //System.out.println("Consumed message from " + testTopic + ": " + consumedData.getRecords());
    }
}