package com.golive.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
@Slf4j
public class EmailService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${mail.from.address}")
    private String fromAddress;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendPasswordResetEmail(String to, String token) {
        if (to == null || to.trim().isEmpty()) {
            log.warn("⚠️ Intento de enviar email a dirección vacía");
            throw new RuntimeException("Email de destino no puede estar vacío");
        }

        try {
            log.info("📧 Iniciando envío de email de recuperación a: {}", to);
            
            // Construir el enlace de restablecimiento usando la URL del frontend
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String subject = "Restablece tu contraseña - GoLive";
            String text = "Hola,\n\n" +
                    "Recibimos una solicitud para restablecer tu contraseña. " +
                    "Haz clic en el siguiente enlace para cambiar tu contraseña:\n\n" +
                    resetLink + "\n\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Saludos,\n" +
                    "El equipo de GoLive";

            log.debug("📋 Detalles del email:");
            log.debug("   From: {}", fromAddress);
            log.debug("   To: {}", to);
            log.debug("   Subject: {}", subject);

            sendViaAPI(to, subject, text);
            log.info("✅ Email de recuperación enviado exitosamente a: {}", to);
        } catch (Exception e) {
            log.error("❌ Error al enviar email a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    private void sendViaAPI(String to, String subject, String text) throws Exception {
        // Construir el payload JSON para SendGrid
        Map<String, Object> payload = new HashMap<>();
        
        // From
        Map<String, String> fromMap = new HashMap<>();
        fromMap.put("email", fromAddress);
        fromMap.put("name", fromName);
        payload.put("from", fromMap);
        
        // To
        List<Map<String, String>> toList = new ArrayList<>();
        Map<String, String> toMap = new HashMap<>();
        toMap.put("email", to);
        toList.add(toMap);
        payload.put("personalizations", Collections.singletonList(Collections.singletonMap("to", toList)));
        
        // Subject
        payload.put("subject", subject);
        
        // Content
        List<Map<String, String>> contentList = new ArrayList<>();
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("type", "text/plain");
        contentMap.put("value", text);
        contentList.add(contentMap);
        payload.put("content", contentList);

        // Convertir a JSON
        String jsonPayload = objectMapper.writeValueAsString(payload);
        log.debug("📤 Payload SendGrid: {}", jsonPayload);

        // Crear la solicitud HTTP
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SENDGRID_API_URL))
                .header("Authorization", "Bearer " + sendGridApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Enviar solicitud
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        log.debug("📬 Respuesta SendGrid - Status: {}", response.statusCode());
        log.debug("📬 Respuesta SendGrid - Body: {}", response.body());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorMsg = String.format("Error SendGrid: %d - %s", response.statusCode(), response.body());
            log.error("❌ {}", errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }
}
