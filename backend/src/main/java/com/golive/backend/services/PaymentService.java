package com.golive.backend.services;

import com.golive.backend.dto.TicketInfoDto;
import com.golive.backend.dto.payment.AttendeeRequest;
import com.golive.backend.dto.payment.PayPalCaptureRequest;
import com.golive.backend.dto.payment.PaymentResultResponse;
import com.golive.backend.dto.payment.TicketSelectionRequest;
import com.golive.backend.model.Event;
import com.golive.backend.model.Ticket;
import com.golive.backend.model.Transaction;
import com.golive.backend.model.User;
import com.golive.backend.repository.EventRepository;
import com.golive.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final EventRepository eventRepository;
    private final TransactionRepository transactionRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final AuthService authService;
    private final TicketEmailService ticketEmailService;

    @Value("${app.payments.service-fee-per-ticket:1.5}")
    private double serviceFeePerTicket;

    public PaymentResultResponse registerPayPalPayment(PayPalCaptureRequest request, String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Se requiere un token válido para registrar el pago");
        }

        validateRequest(request);
        log.info("[Payments] Procesando captura PayPal orderId={} subtotal={} total={} tickets={}",
                request.getProviderOrderId(), request.getSubtotal(), request.getTotal(),
                request.getTickets() != null ? request.getTickets().size() : 0);

        Optional<Transaction> existingTransaction = transactionRepository.findByProviderOrderId(request.getProviderOrderId());
        if (existingTransaction.isPresent()) {
            log.info("[Payments] Orden {} ya registrada, devolviendo transacción {}", request.getProviderOrderId(), existingTransaction.get().getId());
            List<Ticket> existingTickets = ticketService.getTicketsByTransactionId(existingTransaction.get().getId());
            return new PaymentResultResponse(existingTransaction.get(), existingTickets);
        }

        User user = resolveUser(authorizationHeader, request);
        log.debug("[Payments] Usuario resuelto userId={} email={}", user.getId(), user.getEmail());
        Event event = loadEvent(request.getEventId());
        log.debug("[Payments] Evento cargado eventId={} title={}", event.getId(), event.getTitle());

        Map<String, Event.Zone> zoneMap = buildZoneMap(event);
        ValidationResult validationResult = validateInventoryAndTotals(request, zoneMap);
        log.info("[Payments] Validación completada orderId={} totalTickets={} total={}",
                request.getProviderOrderId(), validationResult.totalTickets(), validationResult.total());

        updateInventory(event, request.getTickets(), zoneMap, validationResult.totalTickets());
        log.debug("[Payments] Inventario actualizado eventId={} remaining={}", event.getId(), event.getAvailableTickets());

        Transaction transaction = buildTransaction(request, user, event, validationResult);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("[Payments] Transacción guardada id={} orderId={}", savedTransaction.getId(), savedTransaction.getProviderOrderId());

        List<Ticket> ticketsToPersist = buildTickets(savedTransaction, event, request.getAttendees(), zoneMap);
        List<Ticket> savedTickets = ticketService.saveAll(ticketsToPersist);
        log.info("[Payments] {} tickets generados para transaction {}", savedTickets.size(), savedTransaction.getId());

        savedTransaction.setTicketIds(savedTickets.stream().map(Ticket::getId).collect(Collectors.toList()));
        transactionRepository.save(savedTransaction);

        try {
            List<TicketInfoDto> ticketInfoList = new ArrayList<>();
            int idx = 1;
            for (Ticket ticket : savedTickets) {
                TicketInfoDto dto = new TicketInfoDto();
                dto.setTicketId(ticket.getId());
                dto.setEventName(ticket.getEventTitle());
                dto.setCode(ticket.getTicketNumber());
                dto.setNombre(ticket.getAttendee() != null ? ticket.getAttendee().getFullName() : user.getName());
                dto.setDni(ticket.getAttendee() != null ? ticket.getAttendee().getIdNumber() : "");
                dto.setPrecio(String.format("%.2f", ticket.getPrice() + ticket.getServiceFee()));
                // dto.setIncluye(null); // No poner nada
                dto.setUbicacion(ticket.getVenue() + " - " + ticket.getLocation());
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                dto.setFecha(ticket.getEventDate() != null ? sdf.format(ticket.getEventDate()) : "");
                dto.setQrContent(ticket.getQrCode());
                ticketInfoList.add(dto);
                idx++;
            }
            String eventImagePath = savedTickets.size() > 0 ? savedTickets.get(0).getEventImage() : null;
            ticketEmailService.sendTicketsEmail(user.getEmail(), user.getName(), ticketInfoList, eventImagePath);
        } catch (Exception e) {
            log.error("Error enviando email de entradas: {}", e.getMessage(), e);
        }
        return new PaymentResultResponse(savedTransaction, savedTickets);
    }

    private void validateRequest(PayPalCaptureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de pago es requerida");
        }
        if (request.getProviderOrderId() == null || request.getProviderOrderId().isBlank()) {
            throw new IllegalArgumentException("El identificador de la orden de PayPal es obligatorio");
        }
        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            throw new IllegalArgumentException("No se recibieron entradas en la solicitud");
        }
        if (request.getAttendees() == null || request.getAttendees().isEmpty()) {
            throw new IllegalArgumentException("No se recibieron asistentes en la solicitud");
        }
        if (request.getEventId() == null || request.getEventId().isBlank()) {
            throw new IllegalArgumentException("El identificador del evento es obligatorio");
        }

        log.debug("[Payments] Request validada orderId={} attendees={} tickets={}",
                request.getProviderOrderId(),
                request.getAttendees() != null ? request.getAttendees().size() : 0,
                request.getTickets() != null ? request.getTickets().size() : 0);
    }

    private User resolveUser(String authorizationHeader, PayPalCaptureRequest request) {
        String cleanToken = authorizationHeader.replace("Bearer ", "").trim();
        String email = authService.getEmailFromToken(cleanToken);
        if (email != null) {
            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                return user.get();
            }
        }

        if (request.getUserId() != null) {
            Optional<User> user = userService.findUserById(request.getUserId());
            if (user.isPresent()) {
                return user.get();
            }
        }

        if (request.getUserEmail() != null) {
            Optional<User> user = userService.findByEmail(request.getUserEmail());
            if (user.isPresent()) {
                return user.get();
            }
        }

        throw new IllegalArgumentException("No se pudo identificar al usuario que realiza la compra");
    }

    private Event loadEvent(String eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            event = eventRepository.findByEventId(eventId);
        }
        return event.orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    private Map<String, Event.Zone> buildZoneMap(Event event) {
        if (CollectionUtils.isEmpty(event.getZones())) {
            throw new IllegalStateException("El evento no tiene zonas configuradas");
        }

        Map<String, Event.Zone> zones = new HashMap<>();
        for (Event.Zone zone : event.getZones()) {
            zones.put(normalizeZoneKey(zone.getName()), zone);
        }
        return zones;
    }

    private ValidationResult validateInventoryAndTotals(PayPalCaptureRequest request, Map<String, Event.Zone> zoneMap) {
        double calculatedSubtotal = 0;
        int totalTickets = 0;

        for (TicketSelectionRequest selection : request.getTickets()) {
            Event.Zone zone = zoneMap.get(normalizeZoneKey(selection.getName()));
            if (zone == null) {
                throw new IllegalArgumentException("La zona " + selection.getName() + " no existe en el evento");
            }
            if (selection.getQuantity() <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para la zona " + selection.getName());
            }
            if (zone.getAvailableTickets() < selection.getQuantity()) {
                throw new IllegalArgumentException("No hay suficientes entradas disponibles en " + zone.getName());
            }

            calculatedSubtotal += zone.getPrice() * selection.getQuantity();
            totalTickets += selection.getQuantity();
        }

        double expectedCommission = totalTickets * serviceFeePerTicket;
        double insurance = request.getInsurance() != null ? request.getInsurance() : 0;
        double fees = request.getFees() != null ? request.getFees() : 0;

        assertAmount(calculatedSubtotal, request.getSubtotal(), "subtotal");
        assertAmount(expectedCommission, request.getCommission(), "comisión");

        double expectedTotal = calculatedSubtotal + expectedCommission + insurance + fees;
        assertAmount(expectedTotal, request.getTotal(), "total");

        if (request.getAttendees().size() != totalTickets) {
            throw new IllegalArgumentException("El número de asistentes no coincide con la cantidad de entradas");
        }

        return new ValidationResult(totalTickets, calculatedSubtotal, expectedCommission, insurance, fees, expectedTotal);
    }

    private void updateInventory(Event event,
                                 List<TicketSelectionRequest> selections,
                                 Map<String, Event.Zone> zoneMap,
                                 int ticketsPurchased) {
        for (TicketSelectionRequest selection : selections) {
            Event.Zone zone = zoneMap.get(normalizeZoneKey(selection.getName()));
            zone.setAvailableTickets(zone.getAvailableTickets() - selection.getQuantity());
        }

        int remaining = event.getAvailableTickets() - ticketsPurchased;
        event.setAvailableTickets(Math.max(remaining, 0));
        eventRepository.save(event);
    }

    private Transaction buildTransaction(PayPalCaptureRequest request,
                                         User user,
                                         Event event,
                                         ValidationResult validationResult) {
        Transaction transaction = new Transaction();
        transaction.setUserId(user.getId());
        transaction.setUserEmail(user.getEmail());
        transaction.setUserName(user.getName());
        transaction.setPayerEmail(request.getPayerEmail());

        String eventIdentifier = event.getId() != null ? event.getId() : request.getEventId();
        transaction.setEventId(eventIdentifier);
        transaction.setEventTitle(event.getTitle());
        transaction.setEventImage(event.getImage());
        transaction.setVenue(event.getVenue());
        transaction.setLocation(event.getLocation());
        transaction.setEventDate(event.getDate());

        transaction.setSubtotal(validationResult.subtotal());
        transaction.setCommission(validationResult.commission());
        transaction.setInsurance(validationResult.insurance());
        transaction.setFees(validationResult.fees());
        transaction.setTotal(validationResult.total());
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : "EUR");
        transaction.setStatus(normalizeStatus(request.getStatus()));
        transaction.setPaymentMethod("PAYPAL");
        transaction.setProviderOrderId(request.getProviderOrderId());
        transaction.setProviderCaptureId(request.getProviderCaptureId());
        transaction.setProviderPayerId(request.getProviderPayerId());
        transaction.setTotalTickets(validationResult.totalTickets());
        transaction.setServiceFeePerTicket(serviceFeePerTicket);
        transaction.setTransactionId(request.getProviderOrderId() != null ? request.getProviderOrderId() : transaction.getTransactionNumber());
        transaction.setTicketId(request.getProviderCaptureId() != null ? request.getProviderCaptureId() : transaction.getTransactionNumber());
        transaction.setAmount(validationResult.total());
        transaction.setDate(new Date());

        transaction.setItems(buildTransactionItems(request.getTickets(), event));
        transaction.setAttendees(buildAttendeeSummaries(request.getAttendees()));

        return transaction;
    }

    private List<Transaction.TicketItem> buildTransactionItems(List<TicketSelectionRequest> selections, Event event) {
        List<Transaction.TicketItem> items = new ArrayList<>();
        Map<String, Event.Zone> zoneMap = buildZoneMap(event);

        for (TicketSelectionRequest selection : selections) {
            Event.Zone zone = zoneMap.get(normalizeZoneKey(selection.getName()));
            Transaction.TicketItem item = new Transaction.TicketItem();
            item.setName(zone.getName());
            item.setQuantity(selection.getQuantity());
            item.setUnitPrice(zone.getPrice());
            item.setInsurance(selection.isInsurance());
            items.add(item);
        }
        return items;
    }

    private List<Transaction.AttendeeSummary> buildAttendeeSummaries(List<AttendeeRequest> attendees) {
        List<Transaction.AttendeeSummary> summaries = new ArrayList<>();
        for (AttendeeRequest attendee : attendees) {
            Transaction.AttendeeSummary summary = new Transaction.AttendeeSummary();
            summary.setFullName(attendee.getFullName());
            summary.setEmail(attendee.getEmail());
            summary.setTicketType(attendee.getTicketType());
            summary.setInsurance(attendee.isInsurance());
            summary.setIdNumber(attendee.getIdNumber());
            summaries.add(summary);
        }
        return summaries;
    }

    private List<Ticket> buildTickets(Transaction transaction,
                                      Event event,
                                      List<AttendeeRequest> attendees,
                                      Map<String, Event.Zone> zoneMap) {
        List<Ticket> tickets = new ArrayList<>();
        String eventIdentifier = transaction.getEventId();

        for (AttendeeRequest attendeeRequest : attendees) {
            Event.Zone zone = zoneMap.get(normalizeZoneKey(attendeeRequest.getTicketType()));
            if (zone == null) {
                throw new IllegalArgumentException("La zona " + attendeeRequest.getTicketType() + " no existe");
            }

            Ticket ticket = new Ticket();
            ticket.setTicketId(generateTicketId(transaction.getTransactionNumber(), attendeeRequest.getTicketType()));
            ticket.setTransactionId(transaction.getId());
            ticket.setUserId(transaction.getUserId());
            ticket.setUserEmail(transaction.getUserEmail());
            ticket.setEventId(eventIdentifier);
            ticket.setEventTitle(event.getTitle());
            ticket.setEventImage(event.getImage());
            ticket.setVenue(event.getVenue());
            ticket.setLocation(event.getLocation());
            ticket.setEventDate(event.getDate());
            ticket.setZoneName(zone.getName());
            ticket.setPrice(zone.getPrice());
            ticket.setServiceFee(serviceFeePerTicket);
            ticket.setInsurance(attendeeRequest.isInsurance());
            ticket.setStatus("sold");
            ticket.setDate(new Date());
            ticket.setQrCode(generateQrCode(ticket.getTicketNumber(), transaction.getTransactionNumber()));

            Ticket.Attendee attendee = new Ticket.Attendee();
            attendee.setFullName(attendeeRequest.getFullName());
            attendee.setEmail(attendeeRequest.getEmail());
            attendee.setPhone(attendeeRequest.getPhone());
            attendee.setBirthDay(attendeeRequest.getBirthDay());
            attendee.setBirthMonth(attendeeRequest.getBirthMonth());
            attendee.setBirthYear(attendeeRequest.getBirthYear());
            attendee.setIdType(attendeeRequest.getIdType());
            attendee.setIdNumber(attendeeRequest.getIdNumber());
            attendee.setPostalCode(attendeeRequest.getPostalCode());
            attendee.setCountry(attendeeRequest.getCountry());
            attendee.setTicketType(attendeeRequest.getTicketType());
            attendee.setInsurance(attendeeRequest.isInsurance());
            ticket.setAttendee(attendee);

            tickets.add(ticket);
        }

        return tickets;
    }

    private String normalizeZoneKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "pending";
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "pending";
        }
        return normalized;
    }

    private void assertAmount(double expected, Double provided, String label) {
        if (provided == null) {
            throw new IllegalArgumentException("El valor de " + label + " es obligatorio");
        }
        if (Math.abs(expected - provided) > 0.5) {
            throw new IllegalArgumentException("El " + label + " no coincide con el cálculo del servidor");
        }
    }

    private String generateQrCode(String ticketNumber, String transactionNumber) {
        String payload = ticketNumber + "|" + transactionNumber + "|" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String generateTicketId(String transactionNumber, String ticketType) {
        String base = transactionNumber != null ? transactionNumber : UUID.randomUUID().toString();
        String zone = ticketType != null ? ticketType.replaceAll("\\s+", "-").toUpperCase(Locale.ROOT) : "ZONE";
        return base + "-" + zone + "-" + System.currentTimeMillis();
    }

    private record ValidationResult(int totalTickets,
                                    double subtotal,
                                    double commission,
                                    double insurance,
                                    double fees,
                                    double total) {
    }
}

