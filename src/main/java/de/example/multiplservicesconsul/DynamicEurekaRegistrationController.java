package de.example.multiplservicesconsul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class DynamicEurekaRegistrationController {

    private final RestTemplate restTemplate;

    @Autowired
    public DynamicEurekaRegistrationController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @PostMapping("/registerService")
    public ResponseEntity<String> registerService(@RequestBody Map<String, Object> serviceDetails) {
        String eurekaUrl = "http://localhost:8761/eureka/apps";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(serviceDetails, headers);

        return restTemplate.postForEntity(eurekaUrl, request, String.class);
    }
}

