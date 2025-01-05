package de.example.artemis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ArtemisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtemisApplication.class, args);
    }
}
