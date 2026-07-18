package com.example.atol_integration_service.clients;


import com.example.atol_integration_service.dto.AtolReceiptDto;
import com.example.atol_integration_service.dto.AtolResponseDto;
import com.example.atol_integration_service.dto.AuthRequest;
import com.example.atol_integration_service.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class AtolClient {

    private final RestClient restClient;

    @Value("${atol.api.group-code}")
    private String groupCode;

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
        } catch (RestClientResponseException e) {
            log.error("Ошибка при обращении к АТОЛ: {}", e.getMessage());
            return null;
        }
    }

    public AtolResponseDto sendReceipt(String token, AtolReceiptDto requestBody) {
        log.info("Отправка чека {} на сервер АТОЛ...", requestBody.getExternal_id());
        return restClient.post()
                .uri("/{groupCode}/sell", groupCode)
                .header("Token", token)
                .body(requestBody)
                .retrieve()
                .body(AtolResponseDto.class);
    }
    public AtolResponseDto getReceiptStatus(String uuid, String token) {

        return restClient.get()
                .uri("/{groupCode}/report/{uuid}", groupCode, uuid)
                .header("Token", token)
                .retrieve()
                .body(AtolResponseDto.class);
    }
}