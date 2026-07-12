package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AuthRequest;
import com.example.atol_integration_service.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

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
    private LocalDateTime tokenFetchTime;
    private final Path tokenFilePath = Path.of("token.txt");

    public String getValidToken() {
        if (currentToken == null || tokenFetchTime == null || LocalDateTime.now().isAfter(tokenFetchTime.plusHours(23))) {
            log.info("Токен отсутствует или устарел. Запрашиваем новый...");
            runTokenUpdate();
        }
        return currentToken;
    }

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
            this.tokenFetchTime = LocalDateTime.now();

            try {
                Files.writeString(tokenFilePath, currentToken, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                log.info("Токен успешно получен и сохранен в файл: token.txt");
            } catch (IOException e) {
                log.error("Ошибка при записи токена в файл", e);
            }
        } else if (response.getError() != null) {
            log.error("Ошибка от АТОЛ: {}", response);
        }
    }
}

