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

    @Value("${QUOTE_SERVICE_URL:http://localhost:8081}")
    private String quoteServiceUrl;

    @Value("${VEHICLE_SERVICE_URL:http://localhost:8082}")
    private String vehicleServiceUrl;

    @Value("${AUTH_SERVICE_URL:http://localhost:8090}")
    private String authServiceUrl;

    @Value("${DOCUMENT_SERVICE_URL:http://localhost:8084}")
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
}