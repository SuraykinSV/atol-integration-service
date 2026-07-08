package com.example.atol_integration_service.service;

import com.example.atol_integration_service.dto.AuthRequest;
import com.example.atol_integration_service.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class AuthService {

    private final RestClient restClient;

    @Value("${atol.api.login}")
    private String login;

    @Value("${atol.api.pass}")
    private String pass;

    private String currentToken;

    public AuthService(@Value("${atol.api.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void runTokenUpdate() {
        log.info("Инициализация запроса токена для пользователя: {}", login);

        AuthRequest requestBody = new AuthRequest(login, pass);

        try {
            AuthResponse response = restClient.post()
                    .uri("/getToken")
                    .body(requestBody)
                    .retrieve()
                    .body(AuthResponse.class);

            log.info("Ответ от АТОЛ: {}", response);

            if (response != null && response.getToken() != null && !response.getToken().isBlank()) {
                this.currentToken = response.getToken();
                log.info("Токен успешно получен и сохранен: {}", currentToken);
            } else if (response != null && response.getError() != null && response.getError().getCode() != null) {
                log.error("Ошибка от АТОЛ: Код: [{}], Текст: [{}], Тип: [{}]",
                        response.getError().getCode(), response.getError().getText(), response.getError().getType());
            } else {
                log.error("АТОЛ вернул пустой ответ");
            }

        } catch (RestClientException e) {
            log.error("Критическая ошибка при выполнении HTTP-запроса к АТОЛ: {}", e.getMessage(), e);
        }
    }

}
