package com.golive.backend.services;

import com.golive.backend.model.Ticket;
import com.golive.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public List<Ticket> saveAll(List<Ticket> tickets) {
        return ticketRepository.saveAll(tickets);
    }

    public List<Ticket> getTicketsByUserId(String userId) {
        return ticketRepository.findByUserIdOrderByIssuedAtDesc(userId);
    }

    public List<Ticket> getTicketsByTransactionId(String transactionId) {
        return ticketRepository.findByTransactionId(transactionId);
    }
}

