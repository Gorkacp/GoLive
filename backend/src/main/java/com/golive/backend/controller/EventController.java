package com.golive.backend.controller;

import com.golive.backend.model.Event;
import com.golive.backend.model.User;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService service;
    private final AuthService authService;

    public EventController(EventService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        try {
            List<Event> events = service.getAllEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener eventos: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            var ownerOptional = authService.getUserFromToken(token);
            if (ownerOptional.isEmpty()) {
                return ResponseEntity.status(401).body("Usuario no autorizado");
            }
            User owner = ownerOptional.get();

            event.setCreatedBy(owner.getId());
            event.setCreatedByName(owner.getName());
            event.setCreatedByEmail(owner.getEmail());

            Event createdEvent = service.createEvent(event);
            return ResponseEntity.ok(createdEvent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear evento: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id,
                                         @RequestBody Event event,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Event existing = service.getEventById(id)
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

            if (!authService.canManageEvents(token, existing.getCreatedBy())) {
                return ResponseEntity.status(403).body("No tienes permisos para actualizar este evento");
            }

            event.setCreatedBy(existing.getCreatedBy());
            event.setCreatedByName(existing.getCreatedByName());
            event.setCreatedByEmail(existing.getCreatedByEmail());
            event.setCreatedAt(existing.getCreatedAt());

            Event updatedEvent = service.updateEvent(id, event);
            
            if (updatedEvent != null) {
                return ResponseEntity.ok(updatedEvent);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar evento: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Event existing = service.getEventById(id)
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

            if (!authService.canManageEvents(token, existing.getCreatedBy())) {
                return ResponseEntity.status(403).body("No tienes permisos para eliminar este evento");
            }

            service.deleteEvent(id);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar evento: " + e.getMessage());
        }
    }

    // Endpoint adicional para testing
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("EventController funcionando - " + java.time.LocalDateTime.now());
    }

    @GetMapping("/owner/{userId}")
    public ResponseEntity<?> getEventsByOwner(@PathVariable String userId,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            boolean isSuper = authService.isSuperUser(token);
            if (!isSuper && !authService.canManageEvents(token, userId)) {
                return ResponseEntity.status(403).body("No tienes permisos para consultar estos eventos");
            }

            if (isSuper) {
                return ResponseEntity.ok(service.getAllEvents());
            }

            return ResponseEntity.ok(service.getEventsByOwner(userId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener eventos: " + e.getMessage());
        }
    }

    @GetMapping("/venues")
    public ResponseEntity<?> getUniqueVenues() {
        try {
            List<String> venues = service.getUniqueVenues();
            return ResponseEntity.ok(venues);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener lugares: " + e.getMessage());
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<?> getUniqueLocations() {
        try {
            List<String> locations = service.getUniqueLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener ubicaciones: " + e.getMessage());
        }
    }
}