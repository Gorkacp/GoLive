package com.golive.backend.services;

import com.golive.backend.model.Event;
import com.golive.backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final EventRepository eventRepository;

    // Palabras clave que indican información sensible (no debe responder)
    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
        "ganancia", "ganancias", "revenue", "ingreso", "ingresos", "dinero ganado",
        "beneficio", "beneficios", "profit", "ventas totales", "facturación",
        "soldTickets", "grossRevenue", "occupancy", "vendidas", "vendido"
    );

    // Palabras clave para detectar intenciones
    private static final Map<String, String> INTENT_KEYWORDS;
    
    static {
        Map<String, String> keywords = new HashMap<>();
        keywords.put("evento", "events");
        keywords.put("eventos", "events");
        keywords.put("concierto", "events");
        keywords.put("conciertos", "events");
        keywords.put("festival", "events");
        keywords.put("festivales", "events");
        keywords.put("comprar", "purchase");
        keywords.put("entrada", "purchase");
        keywords.put("entradas", "purchase");
        keywords.put("precio", "purchase");
        keywords.put("pago", "purchase");
        keywords.put("mis entradas", "tickets");
        keywords.put("ticket", "tickets");
        keywords.put("reserva", "tickets");
        keywords.put("contacto", "contact");
        keywords.put("soporte", "contact");
        keywords.put("ayuda", "contact");
        keywords.put("problema", "contact");
        INTENT_KEYWORDS = Collections.unmodifiableMap(keywords);
    }

    public ChatService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public String processMessage(String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();

        // Verificar si el mensaje contiene información sensible
        if (containsSensitiveKeywords(lowerMessage)) {
            return "Lo siento, no puedo proporcionar información sobre ganancias, ingresos o datos financieros. " +
                   "Puedo ayudarte con información sobre eventos, compra de entradas, tus entradas o contacto. ¿En qué más puedo ayudarte?";
        }

        // Detectar intención
        String intent = detectIntent(lowerMessage);

        switch (intent) {
            case "events":
                return getEventsResponse(lowerMessage);
            case "purchase":
                return getPurchaseResponse();
            case "tickets":
                return getTicketsResponse();
            case "contact":
                return getContactResponse();
            case "greeting":
                return getGreetingResponse();
            default:
                return getDefaultResponse();
        }
    }

    private boolean containsSensitiveKeywords(String message) {
        return SENSITIVE_KEYWORDS.stream().anyMatch(message::contains);
    }

    private String detectIntent(String message) {
        // Verificar saludos
        if (message.contains("hola") || message.contains("buenos") || message.contains("buenas") || 
            message.contains("hi") || message.contains("hello")) {
            return "greeting";
        }

        // Buscar palabras clave de intención
        for (Map.Entry<String, String> entry : INTENT_KEYWORDS.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "default";
    }

    private String getGreetingResponse() {
        List<String> greetings = Arrays.asList(
            "¡Hola! 👋 Soy el asistente de GoLive. ¿En qué puedo ayudarte?",
            "¡Bienvenido a GoLive! 🎵 ¿Necesitas ayuda con algo?",
            "Hola, ¿cómo puedo ayudarte hoy?"
        );
        return greetings.get(new Random().nextInt(greetings.size()));
    }

    private String getEventsResponse(String message) {
        try {
            List<Event> allEvents = eventRepository.findAll();
            
            // Filtrar eventos futuros
            Date now = new Date();
            List<Event> upcomingEvents = allEvents.stream()
                .filter(event -> event.getDate() != null && event.getDate().after(now))
                .sorted(Comparator.comparing(Event::getDate))
                .collect(Collectors.toList());

            if (upcomingEvents.isEmpty()) {
                return "Actualmente no hay eventos disponibles. ¡Vuelve pronto para ver nuevos eventos!";
            }

            // Detectar si pregunta por categoría específica
            String category = detectCategory(message);
            List<Event> filteredEvents = category != null 
                ? upcomingEvents.stream()
                    .filter(e -> category.equalsIgnoreCase(e.getCategory()))
                    .collect(Collectors.toList())
                : upcomingEvents;

            if (filteredEvents.isEmpty()) {
                return "No encontré eventos de esa categoría. Tenemos " + upcomingEvents.size() + 
                       " eventos disponibles. ¿Te interesa algún tipo específico?";
            }

            // Construir respuesta
            StringBuilder response = new StringBuilder();
            if (filteredEvents.size() == 1) {
                Event event = filteredEvents.get(0);
                response.append("Tenemos un evento disponible: **").append(event.getTitle()).append("**\n\n");
                response.append("📍 ").append(event.getVenue() != null ? event.getVenue() : "Venue no especificado").append("\n");
                if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                    response.append("🗺️ ").append(event.getLocation()).append("\n");
                }
                response.append("📅 ").append(formatDate(event.getDate())).append("\n");
                if (event.getZones() != null && !event.getZones().isEmpty()) {
                    double minPrice = event.getZones().stream()
                        .mapToDouble(Event.Zone::getPrice)
                        .min()
                        .orElse(0);
                    response.append("💰 Desde ").append(String.format("%.2f", minPrice)).append(" €\n");
                }
                response.append("\n¿Te interesa? Puedes ver más detalles en la página del evento.");
            } else {
                response.append("Tenemos **").append(filteredEvents.size()).append(" eventos** disponibles");
                if (category != null) {
                    response.append(" de la categoría ").append(category);
                }
                response.append(":\n\n");

                int count = Math.min(filteredEvents.size(), 5);
                for (int i = 0; i < count; i++) {
                    Event event = filteredEvents.get(i);
                    response.append("• **").append(event.getTitle()).append("**\n");
                    response.append("  📍 ").append(event.getVenue() != null ? event.getVenue() : "Venue no especificado").append("\n");
                    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                        response.append("  🗺️ ").append(event.getLocation()).append("\n");
                    }
                    response.append("  📅 ").append(formatDate(event.getDate())).append("\n");
                    if (event.getZones() != null && !event.getZones().isEmpty()) {
                        double minPrice = event.getZones().stream()
                            .mapToDouble(Event.Zone::getPrice)
                            .min()
                            .orElse(0);
                        response.append("  💰 Desde ").append(String.format("%.2f", minPrice)).append(" €\n");
                    }
                    response.append("\n");
                }

                if (filteredEvents.size() > 5) {
                    response.append("Y ").append(filteredEvents.size() - 5).append(" eventos más. ");
                }
                response.append("Puedes ver todos los eventos en la página principal.");
            }

            return response.toString();
        } catch (Exception e) {
            return "Hubo un error al buscar eventos. Por favor, intenta nuevamente o visita la página principal.";
        }
    }

    private String detectCategory(String message) {
        if (message.contains("concierto")) return "concierto";
        if (message.contains("festival")) return "festival";
        return null;
    }

    private String getPurchaseResponse() {
        return "Para comprar entradas, simplemente:\n\n" +
               "1️⃣ Selecciona un evento que te interese\n" +
               "2️⃣ Elige la zona y cantidad de entradas\n" +
               "3️⃣ Completa el pago de forma segura\n\n" +
               "Aceptamos varios métodos de pago. ¿Necesitas ayuda con algún paso específico?";
    }

    private String getTicketsResponse() {
        return "Puedes ver todas tus entradas en la sección **\"Mis entradas\"** de tu perfil.\n\n" +
               "Desde ahí podrás:\n" +
               "• Ver tus entradas digitales\n" +
               "• Descargar el PDF de tus entradas\n" +
               "• Ver el código QR\n\n" +
               "¿Necesitas ayuda para acceder a tus entradas?";
    }

    private String getContactResponse() {
        return "Puedes contactarnos de las siguientes formas:\n\n" +
               "📧 **Email:** soporte@golive.com\n" +
               "💬 **Chat:** Estoy aquí para ayudarte\n\n" +
               "Nuestro equipo de soporte está disponible para ayudarte. ¿Cuál es tu consulta?";
    }

    private String getDefaultResponse() {
        List<String> responses = Arrays.asList(
            "Entiendo. ¿Podrías ser más específico? Puedo ayudarte con eventos, compras, entradas o contacto.",
            "No estoy seguro de entender. ¿Puedes reformular tu pregunta?",
            "Puedo ayudarte con información sobre eventos, compra de entradas, tus entradas o contacto. ¿Qué necesitas?"
        );
        return responses.get(new Random().nextInt(responses.size()));
    }

    public Map<String, Object> getPublicEventsInfo() {
        try {
            List<Event> allEvents = eventRepository.findAll();
            Date now = new Date();
            
            List<Event> upcomingEvents = allEvents.stream()
                .filter(event -> event.getDate() != null && event.getDate().after(now))
                .sorted(Comparator.comparing(Event::getDate))
                .collect(Collectors.toList());

            Map<String, Object> info = new HashMap<>();
            info.put("totalEvents", upcomingEvents.size());
            info.put("categories", upcomingEvents.stream()
                .map(Event::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
            
            // Información pública de eventos (sin datos sensibles)
            List<Map<String, Object>> eventsInfo = upcomingEvents.stream()
                .limit(10)
                .map(event -> {
                    Map<String, Object> eventInfo = new HashMap<>();
                    eventInfo.put("title", event.getTitle());
                    eventInfo.put("venue", event.getVenue());
                    eventInfo.put("location", event.getLocation());
                    eventInfo.put("date", event.getDate());
                    eventInfo.put("category", event.getCategory());
                    
                    // Solo precio mínimo, no información de ventas
                    if (event.getZones() != null && !event.getZones().isEmpty()) {
                        double minPrice = event.getZones().stream()
                            .mapToDouble(Event.Zone::getPrice)
                            .min()
                            .orElse(0);
                        eventInfo.put("minPrice", minPrice);
                    }
                    
                    return eventInfo;
                })
                .collect(Collectors.toList());
            
            info.put("events", eventsInfo);
            return info;
        } catch (Exception e) {
            return Map.of("error", "Error al obtener información de eventos");
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "Fecha no disponible";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm");
            return sdf.format(date);
        } catch (Exception e) {
            try {
                LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e2) {
                return date.toString();
            }
        }
    }
}
