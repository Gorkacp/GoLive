package com.golive.backend.services;

import com.golive.backend.model.Event;
import com.golive.backend.model.Ticket;
import com.golive.backend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository repository;
    private final TicketService ticketService;

    public List<Event> getAllEvents() {
        return repository.findAll();
    }

    public List<Event> getEventsByOwner(String userId) {
        return repository.findByCreatedByOrderByDateDesc(userId);
    }

    public Optional<Event> getEventById(String id) {
        return repository.findById(id);
    }

    public Event createEvent(Event event) {
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(new Date());
        }
        event.setUpdatedAt(new Date());
        return repository.save(event);
    }

    @Transactional
    public Event updateEvent(String id, Event event) {
        // Obtener el evento existente para comparar la fecha
        Optional<Event> existingEventOpt = repository.findById(id);
        if (existingEventOpt.isEmpty()) {
            throw new RuntimeException("Evento no encontrado con id: " + id);
        }
        
        Event existingEvent = existingEventOpt.get();
        Date oldDate = existingEvent.getDate();
        Date newDate = event.getDate();
        
        // Configurar campos del evento
        event.setId(id);
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(existingEvent.getCreatedAt());
        }
        event.setUpdatedAt(new Date());
        
        // Guardar el evento actualizado
        Event updatedEvent = repository.save(event);
        
        // Si la fecha cambió, actualizar todos los tickets relacionados
        if (oldDate != null && newDate != null && !oldDate.equals(newDate)) {
            updateTicketsDate(id, newDate);
            log.info("Fecha del evento {} actualizada de {} a {}. Tickets actualizados.", 
                    id, oldDate, newDate);
        } else if (oldDate == null && newDate != null) {
            // Si no tenía fecha y ahora tiene, también actualizar
            updateTicketsDate(id, newDate);
            log.info("Fecha del evento {} establecida a {}. Tickets actualizados.", 
                    id, newDate);
        }
        
        return updatedEvent;
    }
    
    /**
     * Actualiza la fecha de todos los tickets asociados a un evento
     * Busca tickets tanto por el id como por el eventId del evento
     */
    private void updateTicketsDate(String eventId, Date newDate) {
        try {
            // Buscar tickets por el id del evento
            List<Ticket> ticketsById = ticketService.getTicketsByEventId(eventId);
            
            // También buscar por eventId si el evento tiene uno diferente
            Optional<Event> eventOpt = repository.findById(eventId);
            List<Ticket> ticketsByEventId = List.of();
            if (eventOpt.isPresent()) {
                Event event = eventOpt.get();
                String eventIdField = event.getEventId();
                // Solo buscar por eventId si es diferente del id
                if (eventIdField != null && !eventIdField.equals(eventId)) {
                    ticketsByEventId = ticketService.getTicketsByEventId(eventIdField);
                }
            }
            
            // Combinar ambas listas y eliminar duplicados
            Set<String> processedTicketIds = new HashSet<>();
            List<Ticket> allTickets = new ArrayList<>();
            
            for (Ticket ticket : ticketsById) {
                if (!processedTicketIds.contains(ticket.getId())) {
                    allTickets.add(ticket);
                    processedTicketIds.add(ticket.getId());
                }
            }
            
            for (Ticket ticket : ticketsByEventId) {
                if (!processedTicketIds.contains(ticket.getId())) {
                    allTickets.add(ticket);
                    processedTicketIds.add(ticket.getId());
                }
            }
            
            if (allTickets.isEmpty()) {
                log.debug("No se encontraron tickets para el evento {} (id o eventId)", eventId);
                return;
            }
            
            int updatedCount = 0;
            for (Ticket ticket : allTickets) {
                ticket.setEventDate(newDate);
                updatedCount++;
            }
            
            // Guardar todos los tickets actualizados
            ticketService.saveAll(allTickets);
            log.info("Actualizadas {} entradas para el evento {}", updatedCount, eventId);
        } catch (Exception e) {
            log.error("Error al actualizar las fechas de los tickets del evento {}: {}", 
                    eventId, e.getMessage(), e);
            // No lanzamos la excepción para que la actualización del evento no falle
            // pero registramos el error
        }
    }

    public void deleteEvent(String id) {
        repository.deleteById(id);
    }

    /**
     * Obtiene todos los lugares/recintos únicos de los eventos existentes
     */
    public List<String> getUniqueVenues() {
        List<Event> allEvents = repository.findAll();
        Set<String> uniqueVenues = new HashSet<>();
        for (Event event : allEvents) {
            if (event.getVenue() != null && !event.getVenue().trim().isEmpty()) {
                uniqueVenues.add(event.getVenue().trim());
            }
        }
        return new ArrayList<>(uniqueVenues).stream()
                .sorted()
                .toList();
    }

    /**
     * Obtiene todas las ubicaciones únicas de los eventos existentes
     */
    public List<String> getUniqueLocations() {
        List<Event> allEvents = repository.findAll();
        Set<String> uniqueLocations = new HashSet<>();
        for (Event event : allEvents) {
            if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
                uniqueLocations.add(event.getLocation().trim());
            }
        }
        return new ArrayList<>(uniqueLocations).stream()
                .sorted()
                .toList();
    }
}
