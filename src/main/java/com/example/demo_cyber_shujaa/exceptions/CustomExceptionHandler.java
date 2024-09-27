package com.example.demo_cyber_shujaa.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class CustomExceptionHandler extends Throwable {

    /**
     * This method is used to handle Invalid requests
     *
     * @param ex the exception
     * @return the error data
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Optional<Map<String, String>> handleInvalidArgs(MethodArgumentNotValidException ex) {
        return Optional.of(ex.getBindingResult().getFieldErrors().stream()
                .collect(
                        HashMap::new,
                        (map, fieldError) -> map.put(fieldError.getField(), fieldError.getDefaultMessage()),
                        HashMap::putAll
                ));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CustomException.class)
    public Optional<Map<String, String>> handleInvalidRequestException(CustomException ex) {
        return getErrorData(ex.getMessage(), ex.errorCode(), ex);
    }

    /**
     * This method is used to return a map of error data that is extracted from the exception
     *
     * @param message    the error message
     * @param statusCode the error code
     */
    private Optional<Map<String, String>> getErrorData(String message, int statusCode, Object ex) {
        Map<String, String> error = new HashMap<>();
        Optional.of(ex).ifPresent(map -> {
            error.put("message", message);
            error.put("code", String.valueOf(statusCode));
        });
        return Optional.of(error);
    }
}
