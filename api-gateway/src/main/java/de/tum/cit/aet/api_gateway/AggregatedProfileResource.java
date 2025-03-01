package de.tum.cit.aet.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.tum.cit.aet.api_gateway.Constants.ARTEMIS_SERVICE_ID;

@RestController
public class AggregatedProfileResource {

    @Value("${custom-routing.profileMetadataKey}")
    private String profileMetadataKey;

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
            .flatMap(serviceInstance -> {
                Map<String, String> metadata = serviceInstance.getMetadata();
                if (metadata.isEmpty()) {
                    return Stream.empty();
                }

                String profiles = metadata.get(profileMetadataKey);
                if (profiles == null) {
                    return Stream.empty();
                }

                return Arrays.stream(profiles.split(","));
            })
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
