package com.golive.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // Tomamos la URL del frontend desde application.properties o variable de entorno
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // Email y nombre del remitente (configurado en SendGrid)
    @Value("${mail.from.address}")
    private String fromAddress;

    @Value("${mail.from.name}")
    private String fromName;

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        try {
            // Construir el enlace de restablecimiento usando la URL del frontend
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String subject = "Restablece tu contraseña";
            String text = "Hola,\n\nRecibimos una solicitud para restablecer tu contraseña. "
                    + "Haz clic en el siguiente enlace para cambiar tu contraseña:\n"
                    + resetLink
                    + "\n\nSi no solicitaste este cambio, ignora este correo.\n\n"
                    + "Saludos,\n"
                    + "El equipo de " + fromName;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("✅ Email de recuperación enviado a {} desde {}", to, fromAddress);
        } catch (Exception e) {
            log.error("❌ Error al enviar email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }
}
