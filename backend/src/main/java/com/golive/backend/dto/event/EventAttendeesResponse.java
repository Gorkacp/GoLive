package com.golive.backend.dto.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventAttendeesResponse {

    private String eventId;
    private String title;
    private String venue;
    private String location;
    private Date date;
    private String time;
    private int totalSold;
    private double grossRevenue;
    private List<EventAttendeeDto> attendees = new ArrayList<>();

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public double getGrossRevenue() {
        return grossRevenue;
    }

    public void setGrossRevenue(double grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public List<EventAttendeeDto> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<EventAttendeeDto> attendees) {
        this.attendees = attendees;
    }
}

