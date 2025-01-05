package de.example.artemis;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceDiscoveryConfig {

    @Value("${spring.profiles.active}")
    private String activeProfiles;

    private final EurekaInstanceConfigBean eurekaInstanceConfig;

    public ServiceDiscoveryConfig(EurekaInstanceConfigBean eurekaInstanceConfig) {
        this.eurekaInstanceConfig = eurekaInstanceConfig;
    }

    @PostConstruct
    public void setProfileMetadata() {
        if (activeProfiles.isEmpty()) {
            throw new IllegalArgumentException("No active profiles set. Required for service discovery.");
        }
        eurekaInstanceConfig.getMetadataMap().put("profile", activeProfiles);
    }
}
