package de.example.multiplservicesconsul;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@LoadBalancerClient(name = "ARTEMIS", configuration = CustomLoadBalancerConfig.class)
public class CustomLoadBalancerConfig {
    @Bean
    public ReactorServiceInstanceLoadBalancer customLoadBalancer(
            LoadBalancerClientFactory factory
    ) {
        String serviceId = "artemis";
        return new CustomLoadBalancer(
                factory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class).getObject()
        );
    }
}
