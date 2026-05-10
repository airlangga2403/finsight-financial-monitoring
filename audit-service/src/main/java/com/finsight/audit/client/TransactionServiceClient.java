package com.finsight.audit.client;

import com.finsight.audit.client.dto.AccountNetPositionDto;
import com.finsight.audit.client.dto.ApiResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceClient {

    private final RestTemplate restTemplate;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<AccountNetPositionDto> fetchAccountNetPositions(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.plusDays(1).atStartOfDay().minusNanos(1);

        String url = UriComponentsBuilder
                .fromPath("/api/v1/accounts/net-positions")
                .queryParam("startDate", startOfDay.format(ISO_FORMATTER))
                .queryParam("endDate",   endOfDay.format(ISO_FORMATTER))
                .build()
                .toUriString();

        try {
            ResponseEntity<ApiResponseWrapper<List<AccountNetPositionDto>>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                List<AccountNetPositionDto> positions = response.getBody().getData();
                log.info("Fetched {} account net positions from transaction-service for date {}",
                        positions != null ? positions.size() : 0, date);
                return positions != null ? positions : Collections.emptyList();
            }

            log.warn("transaction-service returned non-success response for net-positions on {}", date);
            return Collections.emptyList();

        } catch (RestClientException ex) {
            log.error("Failed to reach transaction-service at [{}]: {}", url, ex.getMessage());
            throw new IllegalStateException(
                    "Cannot connect to transaction-service. Ensure it is running and healthy.", ex
            );
        }
    }
}
