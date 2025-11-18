package com.golive.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        // Agrupar errores por campo
        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        error -> error.getField(),
                        Collectors.mapping(
                                error -> error.getDefaultMessage(),
                                Collectors.toList()
                        )
                ));
        
        // Agregar errores globales si existen
        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> 
                        fieldErrors.computeIfAbsent("global", k -> new ArrayList<>())
                                .add(error.getDefaultMessage())
                );
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "VALIDATION_ERROR",
                "Los datos proporcionados no son válidos",
                LocalDateTime.now(),
                fieldErrors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
