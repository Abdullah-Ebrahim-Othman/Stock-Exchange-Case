package com.example.stockexchange;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@OpenAPIDefinition(
        info = @Info(
                title = "StockExchange REST API Documentation",
                description = "REST API Documentation",
                version = "v1",
                contact = @Contact(
                        name = "Abdullah Ebrahim Othman",
                        email = "abdullah.othmansaleh@gmail.com\n"
                )
        )
)
public class StockExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockExchangeApplication.class, args);
    }
}
