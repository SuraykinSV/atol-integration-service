package com.example.atol_integration_service;

import com.example.atol_integration_service.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class AtolIntegrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtolIntegrationServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner testAtolConnection(AuthService authService) {
		return args -> authService.runTokenUpdate();
	}
}