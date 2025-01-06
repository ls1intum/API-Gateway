package de.example.artemis;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ServiceDiscoveryConfig {

    private final Environment environment;

    private final EurekaInstanceConfigBean eurekaInstanceConfig;

    public ServiceDiscoveryConfig(Environment environment, EurekaInstanceConfigBean eurekaInstanceConfig) {
        this.environment = environment;
        this.eurekaInstanceConfig = eurekaInstanceConfig;
    }

    @PostConstruct
    public void setProfileMetadata() {
        var activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            throw new IllegalArgumentException("No active profiles set. Required for service discovery.");
        }

        eurekaInstanceConfig.getMetadataMap().put("profile", String.join(",", activeProfiles));
    }
}
