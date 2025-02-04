package de.example.gateway;

import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.HttpProtocol;

import java.time.Duration;

import static de.example.gateway.Constants.ARTEMIS_SERVICE_ID;
import static de.example.gateway.Constants.ARTEMIS_SERVICE_NAME;

@Configuration
@LoadBalancerClient(name = ARTEMIS_SERVICE_NAME, configuration = LoadBalancerConfig.class)
public class LoadBalancerConfig {

    @Bean
    public ReactorServiceInstanceLoadBalancer customLoadBalancer(
            LoadBalancerClientFactory factory,
            ProfilePathStore profilePathStore
    ) {
        return new CustomLoadBalancer(
                factory.getLazyProvider(ARTEMIS_SERVICE_ID, ServiceInstanceListSupplier.class).getObject(),
                profilePathStore
        );
    }

    @Bean
    HttpClientCustomizer http3HttpClientCustomizer() {
        return httpClient ->
            httpClient
                .protocol(HttpProtocol.HTTP3)
                .http3Settings(spec -> spec.idleTimeout(Duration.ofSeconds(5))
                        .maxData(10_000_000)
                        .maxStreamDataBidirectionalLocal(1_000_000));
    }
}
