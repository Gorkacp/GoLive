package com.golive.backend.dto.dashboard;

import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDto {

    private long totalEvents;
    private long upcomingEvents;
    private long totalTicketsSold;
    private double totalRevenue;
    private double netRevenue;
    private double averageOccupancy;
    private EventPerformanceDto topEvent;
    private List<EventPerformanceDto> events = new ArrayList<>();

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getUpcomingEvents() {
        return upcomingEvents;
    }

    public void setUpcomingEvents(long upcomingEvents) {
        this.upcomingEvents = upcomingEvents;
    }

    public long getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(long totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(double netRevenue) {
        this.netRevenue = netRevenue;
    }

    public double getAverageOccupancy() {
        return averageOccupancy;
    }

    public void setAverageOccupancy(double averageOccupancy) {
        this.averageOccupancy = averageOccupancy;
    }

    public EventPerformanceDto getTopEvent() {
        return topEvent;
    }

    public void setTopEvent(EventPerformanceDto topEvent) {
        this.topEvent = topEvent;
    }

    public List<EventPerformanceDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventPerformanceDto> events) {
        this.events = events;
    }
}

