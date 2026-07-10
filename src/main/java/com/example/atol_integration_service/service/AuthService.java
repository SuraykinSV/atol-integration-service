package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AuthRequest;
import com.example.atol_integration_service.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AtolClient atolClient;

    @Value("${atol.api.login}")
    private String login;

    @Value("${atol.api.pass}")
    private String password;

    private String currentToken;

    public void runTokenUpdate() {
        AuthRequest requestBody = new AuthRequest(login, password);

        ResponseEntity<AuthResponse> responseEntity = atolClient.requestToken(requestBody);

        if (responseEntity == null || responseEntity.getBody() == null) {
            log.error("Пустой ответ от АТОЛ");
            return;
        }

        AuthResponse response = responseEntity.getBody();

        if (response.getToken() != null && !response.getToken().isBlank()) {
            this.currentToken = response.getToken();
            log.info("Токен успешно получен и сохранен в сервисе: {}", currentToken);
        } else if (response.getError() != null) {
            log.error("Ошибка от АТОЛ: {}", response);
        } else {
            log.error("Пустой ответ от АТОЛ");
        }
    }



}
