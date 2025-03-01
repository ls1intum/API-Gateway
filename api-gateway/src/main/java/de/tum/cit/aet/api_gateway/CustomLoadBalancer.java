package de.tum.cit.aet.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load balancer configuration that chooses the (Artemis) service instance based on the path of the request and the
 * active profiles of each service instance as taken from the instance metadata.
 * Additionally, performs per-module round-robin load balancing when choosing the respective instance.
 */
public class CustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    @Value("${custom-routing.profileMetadataKey}")
    private String profileMetadataKey;

    private final ServiceInstanceListSupplier serviceInstanceListSupplier;

    private final ProfilePathStore profilePathStore;

    /**
     * Round-robin counters for each respective module.
     * The key is the respective Spring Profile mapped from the path.
     */
    private final Map<String, AtomicInteger> moduleRRCounter = new ConcurrentHashMap<>();

    public CustomLoadBalancer(ServiceInstanceListSupplier serviceInstanceListSupplier, ProfilePathStore profilePathStore) {
        this.serviceInstanceListSupplier = serviceInstanceListSupplier;
        this.profilePathStore = profilePathStore;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return serviceInstanceListSupplier
            .get()
            .next()
            .map(serviceInstances -> {
                if (serviceInstances.isEmpty()) {
                    return new EmptyResponse();
                }

                if (request.getContext() instanceof RequestDataContext lbRequest) {
                    RequestData clientRequest = lbRequest.getClientRequest();
                    String path = clientRequest.getUrl().getPath();

                    ServiceInstance chosen = pickBasedOnPath(serviceInstances, path);
                    if (chosen == null) {
                        return new EmptyResponse();
                    }

                    return new DefaultResponse(chosen);
                } else {
                    return new DefaultResponse(serviceInstances.getFirst());
                }
            });
    }

    /**
     * Picks a service instance based on the path of the request and the active profiles of each service instance.
     */
    private ServiceInstance pickBasedOnPath(
            List<ServiceInstance> serviceInstances, String path) {
        String requiredProfile = profilePathStore.getProfileByPath(path);
        List<ServiceInstance> filteredInstances = serviceInstances.
                stream()
                .filter(serviceInstance ->
                        serviceInstance.getMetadata().getOrDefault(profileMetadataKey, "").contains(requiredProfile)
                ).toList();

        if (!filteredInstances.isEmpty()) {
            AtomicInteger counter = moduleRRCounter.computeIfAbsent(requiredProfile, k -> new AtomicInteger(0));
            return selectRoundRobin(filteredInstances, counter);
        }

        return null;
    }

    /**
     * Selects a service instance using a simple round-robin implementation.
     * <br>
     * <b>Note:</b> If the respective AtomicInteger reaches Integer.MAX_VALUE, it will overflow and start from Integer.MIN_VALUE.
     * This however, is not a concern as the overflow is silently wrapped and the modulo calculation also works for negative values.
     */
    private ServiceInstance selectRoundRobin(List<ServiceInstance> serviceInstances, AtomicInteger counter) {
        int idx = counter.getAndIncrement() % serviceInstances.size();
        return serviceInstances.get(idx);
    }
}
