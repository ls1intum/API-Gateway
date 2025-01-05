package de.example.multiplservicesconsul;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Configuration
public class DynamicRouteConfiguration {

    private final ReactiveDiscoveryClient reactiveDiscoveryClient;

    private final GatewayProperties gatewayProperties;

    public DynamicRouteConfiguration(ReactiveDiscoveryClient reactiveDiscoveryClient, GatewayProperties gatewayProperties) {
        this.reactiveDiscoveryClient = reactiveDiscoveryClient;
        this.gatewayProperties = gatewayProperties;
    }

    @Bean
    public RouteDefinitionLocator dynamicRouteDefinitionLocator() {
        return () -> {
            return reactiveDiscoveryClient.getServices()
                    .flatMap(serviceId -> reactiveDiscoveryClient.getInstances(serviceId)
                            .mapNotNull(instance -> buildRouteDefinition(serviceId, instance))
                    )
                    .filter(Objects::nonNull);
        };
    }

    private RouteDefinition buildRouteDefinition(String serviceId, ServiceInstance instance) {
        // Check metadata for certain tags/profiles
        Map<String, String> metadata = instance.getMetadata();

        // For example, if your instance has `metadata.profile = "text"`
        if (metadata.containsKey("profile")) {
            String profile = metadata.get("profile");

            // Build a route that matches /<profile>/** and points to lb://<serviceId>
            RouteDefinition rd = new RouteDefinition();
            rd.setId(serviceId + "-" + profile);
            rd.setUri(URI.create("lb://" + serviceId));

            // Create a Path predicate
            PredicateDefinition pathPredicate = new PredicateDefinition();
            pathPredicate.setName("Path");
            pathPredicate.addArg(NameUtils.generateName(0), "/" + profile + "/**");

            rd.getPredicates().add(pathPredicate);
            return rd;
        }

        // If we don't want a route for this instance, return null (or do something else)
        return null;
    }
}
