package com.golive.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Tomamos la URL del frontend desde application.properties o variable de entorno
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String to, String token) {
        // Construir el enlace de restablecimiento usando la URL del frontend
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String subject = "Restablece tu contraseña";
        String text = "Hola,\n\nRecibimos una solicitud para restablecer tu contraseña. "
                + "Haz clic en el siguiente enlace para cambiar tu contraseña:\n"
                + resetLink
                + "\n\nSi no solicitaste este cambio, ignora este correo.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
