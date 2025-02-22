package de.tum.cit.aet.api_gateway;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.aet.api_gateway.Constants.ARTEMIS_SERVICE_ID;

@RestController
public class AggregatedProfileResource {

    private final DiscoveryClient discoveryClient;

    private final ProfilePathStore profilePathStore;

    public AggregatedProfileResource(DiscoveryClient discoveryClient, ProfilePathStore profilePathStore) {
        this.discoveryClient = discoveryClient;
        this.profilePathStore = profilePathStore;
    }

    /**
     * Returns the set of activated profiles for the Artemis service instances.
     * Aggregated across the profiles of all registered instances.
     */
    @GetMapping("/profiles")
    public Set<String> activatedProfiles() {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(ARTEMIS_SERVICE_ID);

        return serviceInstances.stream()
            .flatMap(serviceInstance -> Arrays.stream(serviceInstance.getMetadata().get(profilePathStore.getDefaultProfile()).split(",")))
            .collect(Collectors.toSet());
    }

    /**
     * Returns the list of services registered with the discovery client.
     */
    @GetMapping("/services")
    public List<String> services() {
        discoveryClient.probe();
        return discoveryClient.getServices();
    }
}
