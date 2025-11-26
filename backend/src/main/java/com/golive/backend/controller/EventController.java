package com.golive.backend.controller;

import com.golive.backend.model.Event;
import com.golive.backend.services.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
        System.out.println("🎯 EVENT CONTROLLER: Controller inicializado");
    }

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        System.out.println("🎯 EVENT CONTROLLER: GET /events - Solicitando todos los eventos");
        
        try {
            System.out.println("🎯 EVENT CONTROLLER: Llamando a service.getAllEvents()");
            List<Event> events = service.getAllEvents();
            System.out.println("✅ EVENT CONTROLLER: Service devolvió " + events.size() + " eventos");
            
            if (events.isEmpty()) {
                System.out.println("ℹ️ EVENT CONTROLLER: No hay eventos en la base de datos");
            } else {
                System.out.println("📋 EVENT CONTROLLER: Primer evento: " + 
                    (events.get(0).getTitle() != null ? events.get(0).getTitle() : "Sin título"));
            }
            
            return ResponseEntity.ok(events);
            
        } catch (Exception e) {
            System.out.println("❌ EVENT CONTROLLER: ERROR en getAllEvents(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al obtener eventos: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        System.out.println("🎯 EVENT CONTROLLER: POST /events - Creando nuevo evento");
        System.out.println("📝 EVENT CONTROLLER: Datos del evento:");
        System.out.println("   - Título: " + (event.getTitle() != null ? event.getTitle() : "NULL"));
        System.out.println("   - Lugar: " + (event.getVenue() != null ? event.getVenue() : "NULL"));
        System.out.println("   - Fecha: " + (event.getDate() != null ? event.getDate() : "NULL"));
        System.out.println("   - Zonas: " + (event.getZones() != null ? event.getZones().size() : "0"));
        
        try {
            System.out.println("🎯 EVENT CONTROLLER: Llamando a service.createEvent()");
            Event createdEvent = service.createEvent(event);
            System.out.println("✅ EVENT CONTROLLER: Evento creado exitosamente");
            System.out.println("🆔 EVENT CONTROLLER: ID del evento creado: " + 
                (createdEvent.getId() != null ? createdEvent.getId() : "NULL"));
            
            return ResponseEntity.ok(createdEvent);
            
        } catch (Exception e) {
            System.out.println("❌ EVENT CONTROLLER: ERROR en createEvent(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al crear evento: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @RequestBody Event event) {
        System.out.println("🎯 EVENT CONTROLLER: PUT /events/" + id + " - Actualizando evento");
        System.out.println("📝 EVENT CONTROLLER: Nuevos datos:");
        System.out.println("   - Título: " + (event.getTitle() != null ? event.getTitle() : "NULL"));
        System.out.println("   - Lugar: " + (event.getVenue() != null ? event.getVenue() : "NULL"));
        
        try {
            System.out.println("🎯 EVENT CONTROLLER: Llamando a service.updateEvent()");
            Event updatedEvent = service.updateEvent(id, event);
            
            if (updatedEvent != null) {
                System.out.println("✅ EVENT CONTROLLER: Evento actualizado exitosamente");
                return ResponseEntity.ok(updatedEvent);
            } else {
                System.out.println("❌ EVENT CONTROLLER: Evento con ID " + id + " no encontrado");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.out.println("❌ EVENT CONTROLLER: ERROR en updateEvent(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al actualizar evento: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id) {
        System.out.println("🎯 EVENT CONTROLLER: DELETE /events/" + id + " - Eliminando evento");
        
        try {
            System.out.println("🎯 EVENT CONTROLLER: Llamando a service.deleteEvent()");
            service.deleteEvent(id);
            System.out.println("✅ EVENT CONTROLLER: Evento eliminado exitosamente");
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            System.out.println("❌ EVENT CONTROLLER: ERROR en deleteEvent(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al eliminar evento: " + e.getMessage());
        }
    }

    // Endpoint adicional para testing
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("🎯 EVENT CONTROLLER: GET /events/test - Endpoint de prueba");
        return ResponseEntity.ok("✅ EventController funcionando - " + java.time.LocalDateTime.now());
    }
}