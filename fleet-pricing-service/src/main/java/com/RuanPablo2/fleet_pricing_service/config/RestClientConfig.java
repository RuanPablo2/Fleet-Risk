package com.RuanPablo2.fleet_pricing_service.config;

import com.RuanPablo2.fleet_pricing_service.clients.VehicleClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Bean
    public VehicleClient vehicleClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8082/api/v1/vehicles")
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(VehicleClient.class);
    }
}