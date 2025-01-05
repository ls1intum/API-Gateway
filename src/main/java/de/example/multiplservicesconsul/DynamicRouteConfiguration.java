package de.example.multiplservicesconsul;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.*;

// ToDo: Think if even required or could be hardcoded
// @Configuration
public class DynamicRouteConfiguration {

    private final ReactiveDiscoveryClient reactiveDiscoveryClient;

    private final DiscoveryLocatorProperties discoveryLocatorProperties;

    public DynamicRouteConfiguration(ReactiveDiscoveryClient reactiveDiscoveryClient, DiscoveryLocatorProperties discoveryLocatorProperties) {
        this.reactiveDiscoveryClient = reactiveDiscoveryClient;
        this.discoveryLocatorProperties = discoveryLocatorProperties;
    }

    @Bean
    public RouteDefinitionLocator dynamicRouteDefinitionLocator() {
        DiscoveryClientRouteDefinitionLocator defaultLocator =
                new DiscoveryClientRouteDefinitionLocator(reactiveDiscoveryClient, discoveryLocatorProperties);

        Flux<RouteDefinition> customFlux = reactiveDiscoveryClient.getServices()
                .flatMap(serviceId -> reactiveDiscoveryClient.getInstances(serviceId)
                        .flatMapIterable(instance -> buildRouteDefinitions(serviceId, instance)));

        return () -> Flux.merge(
                defaultLocator.getRouteDefinitions(),
                customFlux
        );
    }

    private List<RouteDefinition> buildRouteDefinitions(String serviceId, ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();

        // If there's no 'profile' key, return an empty list
        if (!metadata.containsKey("profile")) {
            return Collections.emptyList();
        }

        // Suppose 'profile' is a comma-separated list, e.g. "text,quiz"
        String profileString = metadata.get("profile");
        String[] profiles = profileString.split(",");

        List<RouteDefinition> routeDefinitions = new ArrayList<>();

        for (String rawProfile : profiles) {
            String profile = rawProfile.trim();
            if (!profile.isEmpty()) {
                // Build a route for this particular profile
                RouteDefinition rd = new RouteDefinition();
                rd.setId(serviceId + "-" + profile);
                rd.setUri(URI.create("lb://" + serviceId));

                // Create a Path predicate for "/profile/**"
                PredicateDefinition pathPredicate = new PredicateDefinition();
                pathPredicate.setName("Path");
                pathPredicate.addArg(NameUtils.generateName(0), "/api/" + profile + "/**");

                rd.getPredicates().add(pathPredicate);
                routeDefinitions.add(rd);
            }
        }
        return routeDefinitions;
    }

}
