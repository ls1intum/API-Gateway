package de.example.gateway;

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

    private final ServiceInstanceListSupplier serviceInstanceListSupplier;

    /**
     * Round-robin counter for general requests (not starting with /api/**).
     * In theory, this should never be used, as all requests should start with /api/**.
     */
    private final AtomicInteger generalRRCounter = new AtomicInteger(0);

    /**
     * Round-robin counters for requests starting with /api/**.
     * The key is the module prefix extracted from the path.
     */
    private final Map<String, AtomicInteger> moduleRRCounter = new ConcurrentHashMap<>();

    public CustomLoadBalancer(ServiceInstanceListSupplier serviceInstanceListSupplier) {
        this.serviceInstanceListSupplier = serviceInstanceListSupplier;
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
                    var clientRequest = lbRequest.getClientRequest();
                    String path = clientRequest.getUrl().getPath();

                    ServiceInstance chosen = pickBasedOnPath(serviceInstances, path);

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
        List<ServiceInstance> filteredInstances;
        AtomicInteger counter;

        if (path != null && path.startsWith("/api/")) {
            var moduleServicePrefix = path.split("/")[2];
            filteredInstances = serviceInstances.stream().filter(serviceInstance -> serviceInstance.getMetadata().get("profile").contains(moduleServicePrefix)).toList();
            counter = moduleRRCounter.getOrDefault(moduleServicePrefix, new AtomicInteger(0));
            moduleRRCounter.put(moduleServicePrefix, counter);
        } else {
            // This is a safeguard and should never happen
            filteredInstances = serviceInstances;
            counter = generalRRCounter;
        }

        return selectRoundRobin(filteredInstances, counter);
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
