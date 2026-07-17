package com.example.atol_integration_service.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final ValidationException e) throws UnsupportedEncodingException {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST; // 400
        ErrorResponse errorResponse = new ErrorResponse();
        log.error("{} {}", httpStatus.value(), e.getMessage());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out, true, "UTF-8"));
        String stackTrace = out.toString(Charset.forName("UTF-8"));

        errorResponse.setMessage(e.getMessage());
        errorResponse.setReason(httpStatus.getReasonPhrase());
        errorResponse.setStatus(httpStatus.name());
        errorResponse.setTimestamp(LocalDateTime.now().format(FORMATTER));
        errorResponse.setErrors(List.of(stackTrace));
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final MethodArgumentNotValidException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST; // 400
        ErrorResponse errorResponse = new ErrorResponse();
        log.error("{} Ошибка валидации входящего JSON", httpStatus.value());

        List<String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> "Поле '" + error.getField() + "': " + error.getDefaultMessage())
                .collect(Collectors.toList());

        errorResponse.setMessage("Некорректные данные в запросе");
        errorResponse.setReason(httpStatus.getReasonPhrase());
        errorResponse.setStatus(httpStatus.name());
        errorResponse.setTimestamp(LocalDateTime.now().format(FORMATTER));
        errorResponse.setErrors(fieldErrors);

        return errorResponse;
    }



}
