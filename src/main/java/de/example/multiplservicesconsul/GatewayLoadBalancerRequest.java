package de.example.multiplservicesconsul;

import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.web.server.ServerWebExchange;

public class GatewayLoadBalancerRequest extends DefaultRequest<ServerWebExchange> {
    public GatewayLoadBalancerRequest(ServerWebExchange exchange) {
        super(exchange);
    }
}

