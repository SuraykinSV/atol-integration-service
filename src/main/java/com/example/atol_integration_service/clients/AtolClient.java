package com.example.atol_integration_service.clients;


import com.example.atol_integration_service.dto.AuthRequest;
import com.example.atol_integration_service.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class AtolClient {

    private final RestClient restClient;

    public AtolClient(@Value("${atol.api.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ResponseEntity<AuthResponse> requestToken(AuthRequest requestBody) {
        try {
            log.info("Отправка HTTP-запроса в АТОЛ для получения токена...");
            return restClient.post()
                    .uri("/getToken")
                    .body(requestBody)
                    .retrieve()
                    .toEntity(AuthResponse.class);
        } catch (RestClientException e) {
            log.error("Ошибка при обращении к АТОЛ: {}", e.getMessage());
            return null;
        }
    }
}