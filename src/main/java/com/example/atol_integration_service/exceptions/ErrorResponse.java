package com.example.atol_integration_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String status;
    private String reason;
    private String message;
    private String timestamp;
    private List<String> errors;
}