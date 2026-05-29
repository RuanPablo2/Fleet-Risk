package com.ruanpablo2.fleet_quote_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FleetQuoteServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FleetQuoteServiceApplication.class, args);
	}

}
