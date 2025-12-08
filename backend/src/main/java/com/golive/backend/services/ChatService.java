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
            return "Disculpa, no puedo proporcionar información sobre datos financieros, ganancias o ingresos. " +
                   "Sin embargo, estaré encantado de ayudarte con información sobre nuestros eventos, el proceso de compra de entradas, la gestión de tus entradas o cualquier otra consulta relacionada con nuestros servicios. ¿En qué más puedo asistirte?";
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
            "¡Hola! Soy el asistente virtual de GoLive. Estoy aquí para ayudarte con información sobre nuestros eventos, compra de entradas y cualquier consulta que tengas. ¿En qué puedo asistirte hoy?",
            "Bienvenido a GoLive. Me complace poder ayudarte. ¿Sobre qué te gustaría obtener información?",
            "Hola, gracias por contactar con GoLive. Estoy a tu disposición para ayudarte con eventos, entradas o cualquier otra consulta. ¿Cómo puedo ayudarte?"
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
                return "En este momento no tenemos eventos programados. Te recomendamos visitar nuestra página web periódicamente para estar al tanto de las nuevas incorporaciones a nuestro calendario.";
            }

            // Detectar si pregunta por categoría específica
            String category = detectCategory(message);
            
            // Detectar si pregunta por mes o fecha específica
            Integer targetMonth = detectMonth(message);
            Integer targetYear = detectYear(message);
            
            // Filtrar eventos según criterios
            List<Event> filteredEvents = upcomingEvents;
            
            if (category != null) {
                filteredEvents = filteredEvents.stream()
                    .filter(e -> category.equalsIgnoreCase(e.getCategory()))
                    .collect(Collectors.toList());
            }
            
            if (targetMonth != null) {
                final int month = targetMonth;
                final int year = targetYear != null ? targetYear : Calendar.getInstance().get(Calendar.YEAR);
                filteredEvents = filteredEvents.stream()
                    .filter(e -> {
                        if (e.getDate() == null) return false;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(e.getDate());
                        return cal.get(Calendar.MONTH) + 1 == month && 
                               (targetYear == null || cal.get(Calendar.YEAR) == year);
                    })
                    .collect(Collectors.toList());
            }

            if (filteredEvents.isEmpty()) {
                StringBuilder noResults = new StringBuilder();
                if (category != null || targetMonth != null) {
                    noResults.append("No hemos encontrado eventos");
                    if (category != null) {
                        noResults.append(" de la categoría ").append(category);
                    }
                    if (targetMonth != null) {
                        noResults.append(" en ").append(getMonthName(targetMonth));
                        if (targetYear != null) {
                            noResults.append(" de ").append(targetYear);
                        }
                    }
                    noResults.append(". ");
                }
                noResults.append("Actualmente tenemos ").append(upcomingEvents.size())
                         .append(" eventos disponibles en nuestro calendario. ¿Te gustaría que te muestre todos los eventos o prefieres buscar por otra fecha o categoría?");
                return noResults.toString();
            }

            // Construir respuesta profesional
            StringBuilder response = new StringBuilder();
            
            // Encabezado profesional
            if (filteredEvents.size() == 1) {
                response.append("Perfecto, he encontrado un evento que coincide con tu búsqueda:\n\n");
            } else {
                response.append("He encontrado **").append(filteredEvents.size()).append(" eventos**");
                if (category != null) {
                    response.append(" de la categoría ").append(category);
                }
                if (targetMonth != null) {
                    response.append(" programados para ").append(getMonthName(targetMonth));
                    if (targetYear != null) {
                        response.append(" de ").append(targetYear);
                    }
                }
                response.append(":\n\n");
            }

            int count = Math.min(filteredEvents.size(), 5);
            for (int i = 0; i < count; i++) {
                Event event = filteredEvents.get(i);
                response.append("**").append(event.getTitle()).append("**\n");
                response.append("📍 **Ubicación:** ").append(event.getVenue() != null ? event.getVenue() : "Por determinar").append("\n");
                if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                    response.append("🗺️ **Dirección:** ").append(event.getLocation()).append("\n");
                }
                response.append("📅 **Fecha y hora:** ").append(formatDate(event.getDate())).append("\n");
                if (event.getZones() != null && !event.getZones().isEmpty()) {
                    double minPrice = event.getZones().stream()
                        .mapToDouble(Event.Zone::getPrice)
                        .min()
                        .orElse(0);
                    double maxPrice = event.getZones().stream()
                        .mapToDouble(Event.Zone::getPrice)
                        .max()
                        .orElse(0);
                    if (minPrice == maxPrice) {
                        response.append("💰 **Precio:** ").append(String.format("%.2f", minPrice)).append(" €\n");
                    } else {
                        response.append("💰 **Precios:** Desde ").append(String.format("%.2f", minPrice))
                                .append(" € hasta ").append(String.format("%.2f", maxPrice)).append(" €\n");
                    }
                }
                response.append("\n");
            }

            if (filteredEvents.size() > 5) {
                response.append("_Y ").append(filteredEvents.size() - 5).append(" evento(s) adicional(es)._\n\n");
            }
            
            response.append("Para obtener más información sobre cualquiera de estos eventos o realizar una compra, puedes visitar nuestra página web o indicarme si necesitas ayuda con algo específico.");

            return response.toString();
        } catch (Exception e) {
            return "Disculpa, ha ocurrido un error al procesar tu consulta. Por favor, intenta nuevamente o visita nuestra página web para consultar el calendario de eventos.";
        }
    }

    private String detectCategory(String message) {
        if (message.contains("concierto")) return "concierto";
        if (message.contains("festival")) return "festival";
        return null;
    }

    private Integer detectMonth(String message) {
        // Mapeo de meses en español
        Map<String, Integer> months = new HashMap<>();
        months.put("enero", 1);
        months.put("febrero", 2);
        months.put("marzo", 3);
        months.put("abril", 4);
        months.put("mayo", 5);
        months.put("junio", 6);
        months.put("julio", 7);
        months.put("agosto", 8);
        months.put("septiembre", 9);
        months.put("octubre", 10);
        months.put("noviembre", 11);
        months.put("diciembre", 12);
        
        // También detectar "en marzo", "del mes de", etc.
        for (Map.Entry<String, Integer> entry : months.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Detectar números de mes (1-12)
        for (int i = 1; i <= 12; i++) {
            if (message.contains("/" + i + "/") || 
                message.contains(" " + i + " ") ||
                message.matches(".*\\b" + i + "\\b.*")) {
                return i;
            }
        }
        
        return null;
    }

    private Integer detectYear(String message) {
        // Detectar años (2024, 2025, etc.)
        java.util.regex.Pattern yearPattern = java.util.regex.Pattern.compile("\\b(20\\d{2})\\b");
        java.util.regex.Matcher matcher = yearPattern.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String getMonthName(int month) {
        String[] months = {
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        };
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "";
    }

    private String getPurchaseResponse() {
        return "Para realizar la compra de entradas, el proceso es el siguiente:\n\n" +
               "**1.** Selecciona el evento de tu interés desde nuestro calendario\n" +
               "**2.** Elige la zona y la cantidad de entradas que deseas adquirir\n" +
               "**3.** Completa el proceso de pago de forma segura a través de nuestra plataforma\n\n" +
               "Aceptamos diversos métodos de pago para tu comodidad. Si necesitas asistencia en algún paso específico del proceso de compra, estaré encantado de ayudarte.";
    }

    private String getTicketsResponse() {
        return "Puedes consultar y gestionar todas tus entradas desde la sección **\"Mis entradas\"** en tu perfil de usuario.\n\n" +
               "Desde esta sección tendrás acceso a:\n" +
               "• Visualización de tus entradas digitales\n" +
               "• Descarga del comprobante en formato PDF\n" +
               "• Código QR para el acceso al evento\n\n" +
               "Si necesitas ayuda para acceder a esta sección o tienes alguna consulta sobre tus entradas, estaré encantado de asistirte.";
    }

    private String getContactResponse() {
        return "Puedes ponerte en contacto con nuestro equipo de atención al cliente a través de los siguientes canales:\n\n" +
               "📧 **Correo electrónico:** soporte@golive.com\n" +
               "💬 **Chat en línea:** Estoy disponible para asistirte en este momento\n\n" +
               "Nuestro equipo de soporte está a tu disposición para resolver cualquier consulta o incidencia. Por favor, indícame en qué puedo ayudarte específicamente.";
    }

    private String getDefaultResponse() {
        List<String> responses = Arrays.asList(
            "Agradezco tu consulta. Para poder asistirte de la mejor manera, ¿podrías proporcionarme más detalles? Puedo ayudarte con información sobre eventos, proceso de compra de entradas, gestión de tus entradas o contacto con nuestro equipo de soporte.",
            "Disculpa, no he podido comprender completamente tu consulta. ¿Serías tan amable de reformular tu pregunta? Estoy aquí para ayudarte con eventos, compras, entradas o cualquier otra consulta relacionada con GoLive.",
            "Puedo asistirte con información sobre nuestros eventos, el proceso de compra de entradas, la gestión de tus entradas o ponerte en contacto con nuestro equipo de soporte. ¿Sobre qué aspecto te gustaría obtener información?"
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
