package com.golive.backend.controller;

import com.golive.backend.model.User;
import com.golive.backend.repository.TicketRepository;
import com.golive.backend.repository.UserRepository;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.HashSet;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url:http://localhost:3000}", allowCredentials = "true")
public class NotificationController {

    private final PushNotificationService pushNotificationService;
    private final AuthService authService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Object> request) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Autenticación requerida"));
            }

            var requesterOpt = authService.getUserFromToken(authorizationHeader);
            if (requesterOpt.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Token inválido"));
            }

            User requester = requesterOpt.get();
            boolean isSuper = "super_user".equalsIgnoreCase(requester.getRole());
            boolean isAdmin = "admin".equalsIgnoreCase(requester.getRole());

            if (!isSuper && !isAdmin) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "No tienes permisos para enviar notificaciones"));
            }

            String title = (String) request.get("title");
            String body = (String) request.get("body");
            String url = request.containsKey("url") ? (String) request.get("url") : "/";
            String targetType = (String) request.get("targetType"); // "all", "with_tickets", "event_users"

            if (title == null || title.isBlank() || body == null || body.isBlank()) {
                return ResponseEntity.status(400)
                        .body(Map.of("error", "Título y cuerpo son requeridos"));
            }

            if (targetType == null || targetType.isBlank()) {
                return ResponseEntity.status(400)
                        .body(Map.of("error", "Tipo de destinatario es requerido"));
            }

            int sentCount = 0;

            String eventId = request.containsKey("eventId") ? (String) request.get("eventId") : null;
            
            switch (targetType) {
                case "all":
                    // Enviar a todos los usuarios con suscripciones push
                    pushNotificationService.sendNotificationToAll(title, body, url);
                    sentCount = (int) userRepository.count();
                    break;

                case "with_tickets":
                    // Enviar a usuarios que tienen tickets comprados (de CUALQUIER evento)
                    // El eventId se ignora aquí - siempre obtenemos todos los usuarios con tickets
                    Set<String> userIdsWithTickets = new HashSet<>();
                    try {
                        // Siempre obtener todos los tickets, sin filtrar por evento
                        List<com.golive.backend.model.Ticket> tickets = ticketRepository.findAll();
                        
                        for (com.golive.backend.model.Ticket ticket : tickets) {
                            if (ticket != null) {
                                String userId = ticket.getUserId();
                                if (userId != null && !userId.trim().isEmpty()) {
                                    userIdsWithTickets.add(userId);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error obteniendo usuarios con tickets para notificación: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    for (String userId : userIdsWithTickets) {
                        pushNotificationService.sendNotificationToUser(userId, title, body, url);
                        sentCount++;
                    }
                    break;

                case "event_users":
                    // Enviar a usuarios de un evento específico
                    if (eventId == null || eventId.isBlank()) {
                        return ResponseEntity.status(400)
                                .body(Map.of("error", "eventId es requerido para notificaciones de evento"));
                    }

                    Set<String> eventUserIds = ticketRepository.findByEventId(eventId)
                            .stream()
                            .map(ticket -> ticket.getUserId())
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    
                    for (String userId : eventUserIds) {
                        pushNotificationService.sendNotificationToUser(userId, title, body, url);
                        sentCount++;
                    }
                    break;

                default:
                    return ResponseEntity.status(400)
                            .body(Map.of("error", "Tipo de destinatario no válido"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notificaciones enviadas correctamente",
                    "sentCount", sentCount
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al enviar notificaciones: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getNotificationStats(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Autenticación requerida"));
            }

            var requesterOpt = authService.getUserFromToken(authorizationHeader);
            if (requesterOpt.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Token inválido"));
            }

            User requester = requesterOpt.get();
            boolean isSuper = "super_user".equalsIgnoreCase(requester.getRole());
            boolean isAdmin = "admin".equalsIgnoreCase(requester.getRole());

            if (!isSuper && !isAdmin) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "No tienes permisos para ver estadísticas"));
            }

            long totalUsers = 0;
            try {
                totalUsers = userRepository.count();
            } catch (Exception e) {
                System.err.println("Error obteniendo total de usuarios: " + e.getMessage());
                e.printStackTrace();
                totalUsers = 0;
            }
            
            // Obtener usuarios únicos con tickets
            // Usamos un enfoque simple: obtener todos los tickets y extraer userIds únicos
            Set<String> userIdsWithTickets = new HashSet<>();
            try {
                List<com.golive.backend.model.Ticket> tickets = ticketRepository.findAll();
                if (tickets != null && !tickets.isEmpty()) {
                    for (com.golive.backend.model.Ticket ticket : tickets) {
                        if (ticket != null) {
                            String userId = ticket.getUserId();
                            if (userId != null && !userId.trim().isEmpty()) {
                                userIdsWithTickets.add(userId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error obteniendo usuarios con tickets: " + e.getMessage());
                e.printStackTrace();
                // Si falla, devolver 0 pero no lanzar error para que el endpoint responda
                userIdsWithTickets = new HashSet<>();
            }
            
            long usersWithTickets = userIdsWithTickets.size();
            long usersWithoutTickets = Math.max(0, totalUsers - usersWithTickets);

            return ResponseEntity.ok(Map.of(
                    "totalUsers", totalUsers,
                    "usersWithTickets", usersWithTickets,
                    "usersWithoutTickets", usersWithoutTickets
            ));

        } catch (Exception e) {
            e.printStackTrace(); // Log para debugging
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage(), 
                                 "details", e.getClass().getSimpleName()));
        }
    }
}
