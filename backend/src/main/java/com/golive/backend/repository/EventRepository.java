package com.golive.backend.repository;

import com.golive.backend.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    Optional<Event> findByEventId(String eventId);

    List<Event> findByCreatedByOrderByDateDesc(String createdBy);

}
