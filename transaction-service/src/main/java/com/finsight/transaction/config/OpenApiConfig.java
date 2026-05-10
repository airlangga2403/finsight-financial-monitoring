package com.finsight.transaction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI finsightOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechTest — Audit Service API")
                        .description("""
                                Core microservice responsible for account management, transaction processing,
                                suspicious activity detection, and financial dashboard reporting.
                                
                                **Key Capabilities:**
                                - Account lifecycle management (SAVINGS, CURRENT, INVESTMENT)
                                - Transaction processing with balance enforcement (CREDIT, DEBIT, TRANSFER)
                                - Real-time suspicious activity detection with configurable rules
                                - Dashboard & reporting endpoints for operational monitoring
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tech Test")
                                .email("airlanggapermana96@gmail.com"))
                        .license(new License().name("Internal Use Only")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development")
                ));
    }
}
