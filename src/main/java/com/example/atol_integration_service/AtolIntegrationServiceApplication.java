package com.example.atol_integration_service;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class AtolIntegrationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AtolIntegrationServiceApplication.class, args);
	}

}