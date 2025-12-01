package com.golive.backend.controller;

import com.golive.backend.model.Ticket;
import com.golive.backend.model.User;
import com.golive.backend.dto.event.EventAttendeeDto;
import com.golive.backend.dto.event.EventAttendeesResponse;
import com.golive.backend.model.Event;
import com.golive.backend.model.Ticket;
import com.golive.backend.model.User;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.EventService;
import com.golive.backend.services.TicketService;
import com.golive.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url:http://localhost:3000}", allowCredentials = "true")
public class TicketController {

    private final TicketService ticketService;
    private final AuthService authService;
    private final UserService userService;
    private final EventService eventService;

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

    @GetMapping("/events/{eventId}")
    public ResponseEntity<?> getEventAttendees(@PathVariable String eventId,
                                               @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Autenticación requerida"));
            }

            var requesterOpt = authService.getUserFromToken(authorizationHeader);
            if (requesterOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token inválido"));
            }
            User requester = requesterOpt.get();
            boolean isSuper = "super_user".equalsIgnoreCase(requester.getRole());

            Optional<Event> eventOpt = eventService.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Evento no encontrado"));
            }
            Event event = eventOpt.get();

            if (!isSuper) {
                if (event.getCreatedBy() == null || !event.getCreatedBy().equals(requester.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "No tienes permisos para ver los asistentes de este evento"));
                }
            }

            List<Ticket> tickets = ticketService.getTicketsByEventId(eventId);
            EventAttendeesResponse response = buildAttendeesResponse(event, tickets);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudieron obtener los asistentes", "details", ex.getMessage()));
        }
    }

    /**
     * Exporta los asistentes de un evento a CSV para su descarga desde el backoffice.
     * Solo SUPER_USER o el creador del evento pueden acceder.
     */
    @GetMapping(value = "/events/{eventId}/export", produces = "text/csv")
    public ResponseEntity<?> exportEventAttendeesCsv(@PathVariable String eventId,
                                                     @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Autenticación requerida");
            }

            var requesterOpt = authService.getUserFromToken(authorizationHeader);
            if (requesterOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token inválido");
            }
            User requester = requesterOpt.get();
            boolean isSuper = "super_user".equalsIgnoreCase(requester.getRole());

            Optional<Event> eventOpt = eventService.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Evento no encontrado");
            }
            Event event = eventOpt.get();

            if (!isSuper) {
                if (event.getCreatedBy() == null || !event.getCreatedBy().equals(requester.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para exportar los asistentes de este evento");
                }
            }

            List<Ticket> tickets = ticketService.getTicketsByEventId(eventId);

            String csv = buildAttendeesCsv(event, tickets);

            String filename = "asistentes_" + eventId + ".csv";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .body(csv);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo exportar el CSV: " + ex.getMessage());
        }
    }

    private EventAttendeesResponse buildAttendeesResponse(Event event, List<Ticket> tickets) {
        EventAttendeesResponse response = new EventAttendeesResponse();
        response.setEventId(event.getId());
        response.setTitle(event.getTitle());
        response.setVenue(event.getVenue());
        response.setLocation(event.getLocation());
        response.setDate(event.getDate());
        response.setTime(event.getTime());

        List<EventAttendeeDto> attendees = tickets.stream().map(ticket -> {
            EventAttendeeDto dto = new EventAttendeeDto();
            dto.setTicketId(ticket.getId());
            dto.setTicketNumber(ticket.getTicketNumber());
            dto.setZoneName(ticket.getZoneName());
            dto.setPrice(ticket.getPrice());
            dto.setServiceFee(ticket.getServiceFee());
            dto.setInsurance(ticket.isInsurance());
            dto.setStatus(ticket.getStatus());
            dto.setIssuedAt(ticket.getIssuedAt());
            if (ticket.getAttendee() != null) {
                dto.setAttendeeName(ticket.getAttendee().getFullName());
                dto.setAttendeeEmail(ticket.getAttendee().getEmail());
            } else {
                dto.setAttendeeName(ticket.getUserEmail());
                dto.setAttendeeEmail(ticket.getUserEmail());
            }
            return dto;
        }).collect(Collectors.toList());

        response.setAttendees(attendees);
        response.setTotalSold(attendees.size());
        double gross = attendees.stream()
                .mapToDouble(a -> a.getPrice() + a.getServiceFee())
                .sum();
        response.setGrossRevenue(Math.round(gross * 100.0) / 100.0);
        return response;
    }

    /**
     * Construye un CSV sencillo con los asistentes del evento.
     */
    private String buildAttendeesCsv(Event event, List<Ticket> tickets) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // Cabecera
        sb.append("Evento;Zona;TicketId;TicketNumber;Nombre;Email;Precio;Comision;Seguro;Estado;Emitido\n");

        for (Ticket ticket : tickets) {
            String zone = ticket.getZoneName() != null ? ticket.getZoneName() : "";
            String ticketId = ticket.getId() != null ? ticket.getId() : "";
            String ticketNumber = ticket.getTicketNumber() != null ? ticket.getTicketNumber() : "";

            String attendeeName;
            String attendeeEmail;
            if (ticket.getAttendee() != null) {
                attendeeName = ticket.getAttendee().getFullName();
                attendeeEmail = ticket.getAttendee().getEmail();
            } else {
                attendeeName = ticket.getUserEmail();
                attendeeEmail = ticket.getUserEmail();
            }
            if (attendeeName == null) attendeeName = "";
            if (attendeeEmail == null) attendeeEmail = "";

            double price = ticket.getPrice();
            double serviceFee = ticket.getServiceFee();
            boolean insurance = ticket.isInsurance();
            String status = ticket.getStatus() != null ? ticket.getStatus() : "";
            String issuedAt = ticket.getIssuedAt() != null ? df.format(ticket.getIssuedAt()) : "";

            sb.append(escapeCsv(event.getTitle())).append(';')
                    .append(escapeCsv(zone)).append(';')
                    .append(escapeCsv(ticketId)).append(';')
                    .append(escapeCsv(ticketNumber)).append(';')
                    .append(escapeCsv(attendeeName)).append(';')
                    .append(escapeCsv(attendeeEmail)).append(';')
                    .append(price).append(';')
                    .append(serviceFee).append(';')
                    .append(insurance ? "1" : "0").append(';')
                    .append(escapeCsv(status)).append(';')
                    .append(escapeCsv(issuedAt))
                    .append('\n');
        }

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}

