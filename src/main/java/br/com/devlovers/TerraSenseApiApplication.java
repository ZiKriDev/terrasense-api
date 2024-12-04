package br.com.devlovers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@SpringBootApplication
@OpenAPIDefinition
public class TerraSenseApiApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TerraSenseApiApplication.class, args);
	}
}