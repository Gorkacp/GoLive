package com.golive.backend.controller;

import com.golive.backend.model.PushSubscription;
import com.golive.backend.model.User;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url:http://localhost:3000}", allowCredentials = "true")
public class PushSubscriptionController {

    private final AuthService authService;
    private final PushNotificationService pushNotificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                       @RequestBody Map<String, Object> body,
                                       @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Autenticación requerida"));
            }

            Optional<User> userOpt = authService.getUserFromToken(authorizationHeader);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token inválido"));
            }

            User user = userOpt.get();

            String endpoint = (String) body.get("endpoint");
            @SuppressWarnings("unchecked")
            Map<String, String> keys = (Map<String, String>) body.get("keys");
            Long expirationTime = body.get("expirationTime") instanceof Number
                    ? ((Number) body.get("expirationTime")).longValue()
                    : null;

            if (endpoint == null || endpoint.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Endpoint de suscripción requerido"));
            }

            PushSubscription saved = pushNotificationService.saveOrUpdateSubscription(
                    user.getId(),
                    endpoint,
                    keys,
                    expirationTime,
                    userAgent
            );

            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo registrar la suscripción push", "details", ex.getMessage()));
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, Object> body) {
        try {
            String endpoint = (String) body.get("endpoint");
            if (endpoint == null || endpoint.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Endpoint requerido"));
            }

            pushNotificationService.deleteSubscription(endpoint);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo eliminar la suscripción", "details", ex.getMessage()));
        }
    }
}


