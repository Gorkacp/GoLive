package com.golive.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
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

    public void sendPasswordResetEmail(String to, String token) {
        if (to == null || to.trim().isEmpty()) {
            log.warn("⚠️ Intento de enviar email a dirección vacía");
            throw new RuntimeException("Email de destino no puede estar vacío");
        }

        try {
            log.info("📧 Iniciando envío de email de recuperación a: {}", to);
            
            // Construir el enlace de restablecimiento usando la URL del frontend
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String subject = "Restablece tu contraseña - GoLive";
            String text = "Hola,\n\n" +
                    "Recibimos una solicitud para restablecer tu contraseña. " +
                    "Haz clic en el siguiente enlace para cambiar tu contraseña:\n\n" +
                    resetLink + "\n\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Saludos,\n" +
                    "El equipo de GoLive";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            log.debug("📋 Detalles del email:");
            log.debug("   From: {}", fromAddress);
            log.debug("   To: {}", to);
            log.debug("   Subject: {}", subject);

            mailSender.send(message);
            log.info("✅ Email de recuperación enviado exitosamente a: {}", to);
        } catch (MailException e) {
            log.error("❌ Error de mail al enviar email a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar email a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al enviar email: " + e.getMessage(), e);
        }
    }
}
