package com.example.atol_integration_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String timestamp;

    private ErrorDetails error;

    @Data
    @NoArgsConstructor
    public static class ErrorDetails {
        private String error_id;
        private String code;
        private String text;
        private String type;

    }
}
