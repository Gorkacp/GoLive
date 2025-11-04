// src/main/java/com/golive/backend/service/JwtService.java
package com.golive.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    
    @Value("${jwt.secret:defaultSecretKey}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    // Método temporal - devuelve un token dummy
    public String generateToken(String email) {
        return "dummy-token-" + email + "-" + System.currentTimeMillis();
    }

    public String getEmailFromToken(String token) {
        // Lógica temporal
        if (token.startsWith("dummy-token-")) {
            return token.split("-")[2];
        }
        return null;
    }

    public boolean validateToken(String token) {
        // Temporalmente siempre válido
        return token != null && token.startsWith("dummy-token-");
    }
}