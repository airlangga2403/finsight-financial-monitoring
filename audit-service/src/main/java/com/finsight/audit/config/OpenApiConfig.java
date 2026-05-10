package com.finsight.audit.config;

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
    public OpenAPI auditServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechTest — Audit Service API")
                        .description("""
                                Reconciliation and audit microservice. Calls the transaction-service
                                to fetch account net positions, computes balance integrity,
                                stores reconciliation snapshots, and surfaces discrepancy reports.
                                
                                **Reconciliation Flow:**
                                1. POST /api/v1/reconciliation/run?date=YYYY-MM-DD
                                2. Service calls transaction-service for account net positions
                                3. Evaluates each account for balance discrepancy
                                4. Persists snapshot — BALANCED or DISCREPANCY_FOUND
                                5. Results queryable via history and detail endpoints
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tech Test")
                                .email("airlanggapermana96@gmail.com"))
                        .license(new License().name("Internal Use Only")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local Network")
                ));
    }
}
