package com.golive.backend.services;

import com.golive.backend.model.Event;
import com.golive.backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

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

    public Event updateEvent(String id, Event event) {
        event.setId(id);
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(new Date());
        }
        event.setUpdatedAt(new Date());
        return repository.save(event);
    }

    public void deleteEvent(String id) {
        repository.deleteById(id);
    }
}
