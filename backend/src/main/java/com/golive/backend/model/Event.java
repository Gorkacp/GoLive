package com.golive.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(collection = "events")
public class Event {

    @Id
    private String id;
    
    @JsonProperty("eventId")
    private String eventId;
    
    private String title;
    private String venue;
    private String location;
    
    @JsonDeserialize(using = com.golive.backend.config.DateDeserializer.class)
    private Date date;  // MongoDB espera tipo Date
    private String time;
    private String category;
    private String image;
    private int availableTickets;  // Campo requerido por schema MongoDB
    private List<Zone> zones;

    private String createdBy;
    private String createdByName;
    private String createdByEmail;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

    // Constructor vacío
    public Event() {
        this.eventId = UUID.randomUUID().toString();
    }

    // Clase interna Zone
    public static class Zone {
        private String name;
        private double price;
        private int availableTickets;

        // Constructor vacío
        public Zone() {}

        public Zone(String name, double price, int availableTickets) {
            this.name = name;
            this.price = price;
            this.availableTickets = availableTickets;
        }

        // Getters y Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getAvailableTickets() { return availableTickets; }
        public void setAvailableTickets(int availableTickets) { this.availableTickets = availableTickets; }
    }

    // Getters y Setters de Event
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEventId() { 
        return eventId != null ? eventId : UUID.randomUUID().toString(); 
    }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public int getAvailableTickets() { return availableTickets; }
    public void setAvailableTickets(int availableTickets) { this.availableTickets = availableTickets; }
    
    public List<Zone> getZones() { return zones; }
    public void setZones(List<Zone> zones) { this.zones = zones; }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

} // fin de la clase Event