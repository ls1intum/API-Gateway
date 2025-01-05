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
import java.util.concurrent.atomic.AtomicInteger;

public class CustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final ServiceInstanceListSupplier serviceInstanceListSupplier;

    // One could consider having a RR-counter per profile
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

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

        if (path != null && path.startsWith("/api/")) {
            var moduleServicePrefix = path.split("/")[1];
            filteredInstances = serviceInstances.stream().filter(serviceInstance -> serviceInstance.getMetadata().get("profile").contains(moduleServicePrefix)).toList();
        } else {
            filteredInstances = serviceInstances;
        }

        int idx = roundRobinCounter.getAndIncrement() % filteredInstances.size();
        return filteredInstances.get(idx);
    }
}
