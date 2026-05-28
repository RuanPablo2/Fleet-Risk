package com.ruanpablo2.fleet_gateway.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${route.quote.url}")
    private String quoteServiceUrl;

    @Value("${route.vehicle.url}")
    private String vehicleServiceUrl;

    @Value("${route.auth.url}")
    private String authServiceUrl;

    @Value("${route.document.url}")
    private String documentServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> quoteRoute() {
        System.out.println("🚦 [FLEET GATEWAY] Registering route for Quote Service: " + quoteServiceUrl);

        return route("quote-service-route")
                .route(RequestPredicates.path("/api/v1/quotes/**"), http())
                .before(uri(quoteServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> vehicleRoute() {
        System.out.println("🚦 [FLEET GATEWAY] Registering route for Vehicle Service: " + vehicleServiceUrl);

        return route("vehicle-service-route")
                .route(RequestPredicates.path("/api/v1/vehicles/**"), http())
                .before(uri(vehicleServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authRoute() {
        System.out.println("🚦 [FLEET GATEWAY] Registering route for Auth Service: " + authServiceUrl);

        return route("auth-service-route")
                .route(RequestPredicates.path("/api/v1/auth/**"), http())
                .before(uri(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> documentRoute() {
        System.out.println("🚦 [FLEET GATEWAY] Registering route for Document Service: " + documentServiceUrl);

        return route("document-service-route")
                .route(RequestPredicates.path("/api/v1/documents/**"), http())
                .before(uri(documentServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> websocketQuoteRoute() {
        System.out.println("🚦 [FLEET GATEWAY] Registering route for WebSockets: " + quoteServiceUrl);

        String wsUrl = quoteServiceUrl.replace("http://", "ws://");

        return route("websocket-quote-route")
                .route(RequestPredicates.path("/ws/quotes/**"), http())
                .before(uri(wsUrl))
                .build();
    }
}