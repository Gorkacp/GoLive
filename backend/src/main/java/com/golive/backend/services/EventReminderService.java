package com.golive.backend.services;

import com.golive.backend.model.Event;
import com.golive.backend.model.Ticket;
import com.golive.backend.repository.EventRepository;
import com.golive.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventReminderService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final PushNotificationService pushNotificationService;

    private final ZoneId zoneId = ZoneId.of("Europe/Madrid");

    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Madrid")
    public void sendSameDayReminders() {
        LocalDate today = LocalDate.now(zoneId);
        processRemindersForDate(today, "Hoy es el día del evento");
    }

    @Scheduled(cron = "0 0 18 * * *", zone = "Europe/Madrid")
    public void sendOneDayBeforeReminders() {
        LocalDate targetDate = LocalDate.now(zoneId).plusDays(1);
        processRemindersForDate(targetDate, "Mañana es tu evento");
    }

    private void processRemindersForDate(LocalDate targetDate, String prefix) {
        Instant startOfDay = targetDate.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(zoneId).minusSeconds(1).toInstant();

        Date start = Date.from(startOfDay);
        Date end = Date.from(endOfDay);

        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> e.getDate() != null && !e.getDate().before(start) && !e.getDate().after(end))
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            return;
        }

        Map<String, Event> eventsById = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getEventId() != null && eventsById.containsKey(t.getEventId()))
                .collect(Collectors.toList());

        Map<String, List<Ticket>> ticketsByUser = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getUserId));

        ticketsByUser.forEach((userId, userTickets) -> {
            if (userId == null) {
                return;
            }
            userTickets.stream()
                    .map(t -> eventsById.get(t.getEventId()))
                    .filter(e -> e != null)
                    .distinct()
                    .forEach(event -> {
                        try {
                            String title = prefix + ": " + event.getTitle();
                            String body = "Te esperamos en " + event.getVenue() + " (" + event.getLocation() + ").";
                            String icon = event.getImage();
                            String url = "/misEntradas";
                            pushNotificationService.sendNotificationToUser(userId, title, body, icon, url);
                        } catch (Exception ex) {
                            log.error("Error enviando recordatorio push para usuario {} y evento {}: {}",
                                    userId, event.getId(), ex.getMessage());
                        }
                    });
        });
    }
}


