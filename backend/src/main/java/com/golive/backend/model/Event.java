package com.golive.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "events")
public class Event {

    @Id
    private String id;
    private String title;
    private String venue;
    private String location;
    private String date;
    private String time;
    private String category;
    private String image;
    private List<Zone> zones;

    // Clase interna Zone
    public static class Zone {
        private String name;
        private double price;
        private int availableTickets;

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
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public List<Zone> getZones() { return zones; }
    public void setZones(List<Zone> zones) { this.zones = zones; }

} // fin de la clase Event
