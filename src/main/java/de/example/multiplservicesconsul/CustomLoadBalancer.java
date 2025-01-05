package de.example.multiplservicesconsul;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
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

                    if (request instanceof GatewayLoadBalancerRequest lbRequest) {
                        var exchange = lbRequest.getContext();

                        String path = exchange.getRequest().getURI().getPath();
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
        int idx;

        if (path != null && path.startsWith("/api/")) {
            var moduleServicePrefix = path.split("/")[1];
            filteredInstances = serviceInstances.stream().filter(serviceInstance -> serviceInstance.getMetadata().get("profile").contains(moduleServicePrefix)).toList();
            var counter = moduleRRCounter.getOrDefault(moduleServicePrefix, new AtomicInteger(0));
            moduleRRCounter.put(moduleServicePrefix, counter);
            idx = counter.getAndIncrement() % filteredInstances.size();
        } else {
            filteredInstances = serviceInstances;
            idx = generalRRCounter.getAndIncrement() % filteredInstances.size();
        }

        return filteredInstances.get(idx);
    }
}
