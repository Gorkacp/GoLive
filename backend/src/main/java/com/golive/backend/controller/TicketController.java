package com.golive.backend.controller;

import com.golive.backend.model.Ticket;
import com.golive.backend.model.User;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.TicketService;
import com.golive.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url:http://localhost:3000}", allowCredentials = "true")
public class TicketController {

    private final TicketService ticketService;
    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyTickets(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Autenticación requerida"));
            }

            String email = authService.getEmailFromToken(authorizationHeader.replace("Bearer ", "").trim());
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token inválido"));
            }

            Optional<User> user = userService.findByEmail(email);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            List<Ticket> tickets = ticketService.getTicketsByUserId(user.get().getId());
            return ResponseEntity.ok(tickets);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudieron obtener las entradas", "details", ex.getMessage()));
        }
    }
}

