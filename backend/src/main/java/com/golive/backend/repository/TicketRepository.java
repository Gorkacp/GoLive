package com.golive.backend.repository;

import com.golive.backend.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

    List<Ticket> findByUserIdOrderByIssuedAtDesc(String userId);

    List<Ticket> findByTransactionId(String transactionId);

    List<Ticket> findByEventId(String eventId);
}

