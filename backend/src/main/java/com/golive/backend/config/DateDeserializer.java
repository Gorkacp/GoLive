package com.golive.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateDeserializer extends JsonDeserializer<Date> {
    
    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        String value = jsonParser.getText();
        
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        try {
            // Intentar parsear como fecha ISO (YYYY-MM-DD)
            LocalDate localDate = LocalDate.parse(value);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e1) {
            try {
                // Intentar parsear como ISO DateTime
                return Date.from(java.time.Instant.parse(value));
            } catch (Exception e2) {
                System.err.println("No se pudo parsear la fecha: " + value);
                return new Date();
            }
        }
    }
}
