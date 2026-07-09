package com.example.atol_integration_service.service;

import com.example.atol_integration_service.clients.AtolClient;
import com.example.atol_integration_service.dto.AuthRequest;
import com.example.atol_integration_service.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

        AuthResponse response = atolClient.requestToken(requestBody);

        if (response != null && response.getToken() != null && !response.getToken().isBlank()) {
            this.currentToken = response.getToken();
            log.info("Токен успешно получен и сохранен в сервисе: {}", currentToken);
        } else if (response != null && response.getError() != null) {
            log.error("Ошибка от АТОЛ: Id [{}], Код [{}], Текст [{}], Тип [{}]", response.getError().getError_id(),
                    response.getError().getCode(), response.getError().getText(), response.getError().getType());
        } else {
            log.error("Пустой ответ от АТОЛ");
        }
    }

}
