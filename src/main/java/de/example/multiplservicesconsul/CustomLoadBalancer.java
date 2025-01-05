package de.example.multiplservicesconsul;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final ServiceInstanceListSupplier serviceInstanceListSupplier;

    private final AtomicInteger generalRRCounter = new AtomicInteger(0);
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

                        ServiceInstance chosen = pickBasedOnPathOrAttribute(serviceInstances, path);

                        return new DefaultResponse(chosen);
                    } else {
                        return new DefaultResponse(serviceInstances.getFirst());
                    }
                });
    }

    private ServiceInstance pickBasedOnPathOrAttribute(
            List<ServiceInstance> serviceInstances, String path) {
        List<ServiceInstance> filteredInstances;
        AtomicInteger counter;

        if (path != null && path.startsWith("/api/")) {
            var moduleServicePrefix = path.split("/")[2];
            filteredInstances = serviceInstances.stream().filter(serviceInstance -> serviceInstance.getMetadata().get("profile").contains(moduleServicePrefix)).toList();
            counter = moduleRRCounter.getOrDefault(moduleServicePrefix, new AtomicInteger(0));
            moduleRRCounter.put(moduleServicePrefix, counter);
        } else {
            filteredInstances = serviceInstances;
            counter = generalRRCounter;
        }

        int idx = counter.getAndIncrement() % filteredInstances.size();
        return filteredInstances.get(idx);
    }
}
