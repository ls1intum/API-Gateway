package de.example.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.example.gateway.Constants.ARTEMIS_SERVICE_ID;

@RestController
public class AggregatedProfileResource {

    private final DiscoveryClient discoveryClient;

    public AggregatedProfileResource(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/profiles")
    public Set<String> activatedProfiles() {
        var serviceInstances = discoveryClient.getInstances(ARTEMIS_SERVICE_ID);

        return serviceInstances.stream()
                .flatMap(serviceInstance -> Arrays.stream(serviceInstance.getMetadata().get("profile").split(",")))
                .collect(Collectors.toSet());
    }

    @GetMapping("/services")
    public List<String> services() {
        discoveryClient.probe();
        return discoveryClient.getServices();
    }
}
