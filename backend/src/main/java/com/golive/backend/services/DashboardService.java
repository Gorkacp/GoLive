package com.golive.backend.services;

import com.golive.backend.dto.dashboard.DashboardSummaryDto;
import com.golive.backend.dto.dashboard.EventPerformanceDto;
import com.golive.backend.model.Event;
import com.golive.backend.model.Ticket;
import com.golive.backend.model.Transaction;
import com.golive.backend.repository.EventRepository;
import com.golive.backend.repository.TicketRepository;
import com.golive.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(EventRepository eventRepository,
                            TicketRepository ticketRepository,
                            TransactionRepository transactionRepository) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.transactionRepository = transactionRepository;
    }

    public DashboardSummaryDto buildSummary(String userId, boolean includeAll) {
        List<Event> events = includeAll
                ? eventRepository.findAll()
                : eventRepository.findByCreatedByOrderByDateDesc(userId);

        List<EventPerformanceDto> performances = events.stream()
                .map(this::mapEventPerformance)
                .collect(Collectors.toList());

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setEvents(performances);
        summary.setTotalEvents(performances.size());
        summary.setUpcomingEvents(performances.stream()
                .filter(ep -> ep.getDate() == null || ep.getDate().after(new Date()))
                .count());
        summary.setTotalTicketsSold(performances.stream()
                .mapToLong(EventPerformanceDto::getSoldTickets)
                .sum());
        summary.setTotalRevenue(roundTwoDecimals(performances.stream()
                .mapToDouble(EventPerformanceDto::getGrossRevenue)
                .sum()));
        summary.setNetRevenue(roundTwoDecimals(performances.stream()
                .mapToDouble(EventPerformanceDto::getNetRevenue)
                .sum()));
        summary.setAverageOccupancy(roundTwoDecimals(performances.stream()
                .mapToDouble(EventPerformanceDto::getOccupancy)
                .average()
                .orElse(0)));

        performances.stream()
                .max(Comparator.comparingDouble(EventPerformanceDto::getGrossRevenue))
                .ifPresent(summary::setTopEvent);

        return summary;
    }

    private EventPerformanceDto mapEventPerformance(Event event) {
        List<Ticket> tickets = ticketRepository.findByEventId(event.getId());
        List<Transaction> transactions = transactionRepository.findByEventId(event.getId());

        long soldTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() == null || "sold".equalsIgnoreCase(ticket.getStatus()))
                .count();

        double grossRevenue = tickets.stream()
                .mapToDouble(ticket -> ticket.getPrice() + ticket.getServiceFee())
                .sum();

        double netRevenue = grossRevenue * 0.92; // Margen conservador del 8% para comisiones

        EventPerformanceDto dto = new EventPerformanceDto();
        dto.setEventId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setImage(event.getImage());
        dto.setCategory(event.getCategory());
        dto.setVenue(event.getVenue());
        dto.setLocation(event.getLocation());
        dto.setDate(event.getDate());
        dto.setTime(event.getTime());
        dto.setAvailableTickets(event.getAvailableTickets());
        dto.setSoldTickets((int) soldTickets);
        dto.setGrossRevenue(roundTwoDecimals(grossRevenue));
        dto.setNetRevenue(roundTwoDecimals(netRevenue));
        dto.setTransactions(transactions.size());

        int available = Math.max(event.getAvailableTickets(), 1);
        double occupancy = (soldTickets * 100d) / available;
        dto.setOccupancy(roundTwoDecimals(Math.min(occupancy, 100)));

        return dto;
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

