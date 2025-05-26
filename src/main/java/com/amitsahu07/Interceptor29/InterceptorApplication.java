package com.amitsahu07.Interceptor29;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.kafka.test.EmbeddedKafkaBroker; // Ensure this import is still present if you want to run Kafka here

@SpringBootApplication
public class InterceptorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterceptorApplication.class, args);
    }

    // This Bean is for serving the React frontend correctly
    @Bean
    public WebMvcConfigurer forwardReactApp() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                // For client-side routing, forward all unmatched paths to index.html
                // This ensures React Router handles routes like /app, /login etc.
                registry.addViewController("/app/**").setViewName("forward:/index.html");
                // You might also need to forward the root path, if you want / to also load React
                registry.addViewController("/").setViewName("forward:/index.html");
            }
        };
    }

    // Make sure your EmbeddedKafkaBroker bean is NOT in this project anymore
    // It should ONLY be in your 'embedded-kafka-server' project.
    // If you have an @Bean for EmbeddedKafkaBroker here, REMOVE IT.
    /*
    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        // This should NOT be here anymore for the Interceptor29 project!
        // It's in your separate embedded-kafka-server project.
    }
    */
}