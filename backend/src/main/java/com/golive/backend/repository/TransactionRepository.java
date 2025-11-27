package com.golive.backend.repository;

import com.golive.backend.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Optional<Transaction> findByProviderOrderId(String providerOrderId);

    List<Transaction> findByEventId(String eventId);
}

