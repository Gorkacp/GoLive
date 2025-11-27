package com.golive.backend.services;

import com.golive.backend.model.Ticket;
import com.golive.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public void attachDigitalAssets(String ticketId, byte[] qrImageData, byte[] ticketPdf) {
        if (ticketId == null) {
            return;
        }
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            return;
        }
        Ticket ticket = ticketOpt.get();
        if (qrImageData != null && qrImageData.length > 0) {
            ticket.setQrImageData(qrImageData);
        }
        if (ticketPdf != null && ticketPdf.length > 0) {
            ticket.setTicketPdf(ticketPdf);
        }
        ticketRepository.save(ticket);
    }
}

