package com.ruanpablo2.fleet_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayConfig {

    private String quoteServiceUrl = "http://localhost:8081";

    @Bean
    public RouterFunction<ServerResponse> quoteRoute() {
        System.out.println("🚦 [FLEET GATEWAY] Registering route for Quote Service: " + quoteServiceUrl);

        return route("quote-service-route")
                .route(RequestPredicates.path("/api/quotes/**"), http())
                .before(uri(quoteServiceUrl))
                .build();
    }
}