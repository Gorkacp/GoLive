package com.golive.backend.services;

import com.golive.backend.dto.TicketInfoDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.*;
import jakarta.mail.internet.*;

@Service
public class TicketEmailService {
    @Autowired
    private EmailAttachmentHelper emailAttachmentHelper;

    // Llama a este método tras la compra
    public void sendTicketsEmail(String email, String nombre, List<TicketInfoDto> tickets, String eventImagePath) throws IOException, MessagingException {
        // Adjuntamos PDF, solo título, hero, QR+info y PDF adjunto
        int total = tickets.size();
        int idx = 1;
        // Comprimir imagen del evento si existe (máx 600px ancho), igual que en PDF
        byte[] eventImageBytes = null;
        String eventImageType = "image/jpeg";
        BufferedImage eventImg = null;
        if (eventImagePath != null && !eventImagePath.isEmpty()) {
            try {
                if (eventImagePath.startsWith("http://") || eventImagePath.startsWith("https://")) {
                    java.net.URL url = new java.net.URL(eventImagePath);
                    try (java.io.InputStream in = url.openStream()) {
                        eventImg = ImageIO.read(in);
                    }
                } else {
                    File imgFile = new File(eventImagePath);
                    if (imgFile.exists()) {
                        eventImg = ImageIO.read(imgFile);
                    }
                }
                if (eventImg != null) {
                    int newW = Math.min(600, eventImg.getWidth());
                    int newH = (int) (eventImg.getHeight() * (newW / (double) eventImg.getWidth()));
                    BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2d = scaled.createGraphics();
                    g2d.drawImage(eventImg, 0, 0, newW, newH, null);
                    g2d.dispose();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(scaled, "jpg", baos);
                    eventImageBytes = baos.toByteArray();
                } else {
                    System.err.println("[TicketEmailService] No se pudo cargar la imagen del evento para el email: " + eventImagePath);
                }
            } catch (Exception e) {
                eventImageBytes = null;
                System.err.println("[TicketEmailService] Error cargando imagen del evento para el email: " + eventImagePath + " - " + e.getMessage());
            }
        }
        // Generar PDF y adjuntarlo
        List<InputStreamSource> pdfs = new ArrayList<>();
        for (TicketInfoDto ticket : tickets) {
            ByteArrayOutputStream pdf = generateTicketPdf(ticket, eventImageBytes, eventImageType, idx, total);
            pdfs.add(new ByteArrayResource(pdf.toByteArray()));
            idx++;
        }
        String subject = "Tu entrada para " + (tickets.isEmpty() ? "" : tickets.get(0).getEventName());
        TicketInfoDto ticket = tickets.get(0);
        StringBuilder body = new StringBuilder();
        // Título y saludo
        body.append("<div style='font-family:sans-serif;max-width:700px;margin:auto;background:#fff;border-radius:10px;box-shadow:0 2px 8px #0001;padding:0 0 32px 0;'>");
        body.append("<div style='padding:32px 32px 0 32px;'>");
        body.append("<div style='font-size:22px;font-weight:700;margin-bottom:8px;'>Tu entrada para ").append(ticket.getEventName()).append("</div>");
        body.append("<div style='font-size:16px;color:#222;margin-bottom:24px;'>Hola ").append(nombre).append(", gracias por comprar a través de GoLive.</div>");
        body.append("</div>");
        // Imagen hero del evento (comprimida, inline base64, no como adjunto)
        if (eventImageBytes != null && eventImageBytes.length > 0) {
            try {
                String base64Img = java.util.Base64.getEncoder().encodeToString(eventImageBytes);
                body.append("<img src='data:image/jpeg;base64,").append(base64Img).append("' style='width:100%;max-width:700px;display:block;margin:0 auto 0 auto;border-radius:0;object-fit:cover;height:220px;' alt='Hero evento'/>");
            } catch(Exception e) {
                body.append("<div style='width:100%;height:220px;max-width:700px;background:#eee;display:block;margin:0 auto 0 auto;border-radius:0;'></div>");
            }
        } else {
            body.append("<div style='width:100%;height:220px;max-width:700px;background:#eee;display:block;margin:0 auto 0 auto;border-radius:0;'></div>");
        }
        // Bloque de entrada con QR y datos, margen entre QR e info
        body.append("<div style='padding:40px 0 40px 0;'>");
        body.append("<div style='font-size:20px;font-weight:700;margin-bottom:24px;margin-top:0;text-align:center;'>Tu entrada</div>");
        String qrBase64 = "";
        boolean qrOk = false;
        try {
            BufferedImage qr = generateQr(ticket.getQrContent());
            if (qr != null) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                ImageIO.write(qr, "png", baos);
                qrBase64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
                qrOk = true;
            }
        } catch(Exception e) { qrBase64 = ""; qrOk = false; }
        body.append("<div style='display:flex;align-items:flex-start;gap:48px;max-width:500px;margin:0 auto 0 auto;'>");
        if (qrOk && qrBase64 != null && !qrBase64.isEmpty()) {
            body.append("<div style='min-width:120px;'><img src='data:image/png;base64,").append(qrBase64).append("' style='width:120px;height:120px;border:2px solid #222;border-radius:8px;display:block;background:#fff;' alt='QR entrada'/><div style='font-size:13px;text-align:center;margin-top:8px;letter-spacing:2px;font-weight:600;'>").append(ticket.getCode()).append("</div></div>");
        } else {
            body.append("<div style='min-width:120px;height:120px;background:#eee;border:2px solid #222;border-radius:8px;'></div>");
        }
        body.append("<div style='flex:1;padding-top:0;margin-left:24px;'>");
        body.append("<div style='font-size:17px;font-weight:700;margin-bottom:6px;'>").append(ticket.getEventName()).append("</div>");
        body.append("<div style='font-size:14px;color:#444;margin-bottom:8px;'>").append(ticket.getFecha()).append("</div>");
        body.append("<div style='font-size:14px;margin-bottom:6px;'><b>Precio:</b> ").append(ticket.getPrecio()).append(" €</div>");
        body.append("<div style='font-size:14px;margin-bottom:6px;'><b>Nombre:</b> ").append(ticket.getNombre()).append("</div>");
        body.append("<div style='font-size:14px;margin-bottom:6px;'><b>DNI:</b> ").append(ticket.getDni()).append("</div>");
        body.append("</div></div>");
        body.append("</div>");
        body.append("</div>");
        // Adjuntamos el PDF generado
        emailAttachmentHelper.sendEmailWithAttachments(email, subject, body.toString(), pdfs, "entrada.pdf", null, null);
    }

    private ByteArrayOutputStream generateTicketPdf(TicketInfoDto ticket, byte[] eventImageBytes, String eventImageType, int idx, int total) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);

        // Fondo blanco
        content.setNonStrokingColor(255, 255, 255);
        content.addRect(0, 0, 595, 842);
        content.fill();
        content.setNonStrokingColor(0, 0, 0);

        // Título y fecha del evento (más abajo)
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 22);
        content.newLineAtOffset(40, 770);
        content.showText(ticket.getEventName() != null ? ticket.getEventName() : "");
        content.endText();

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 14);
        content.newLineAtOffset(40, 750);
        content.showText(ticket.getFecha() != null ? ticket.getFecha() : "");
        content.endText();

        // Imagen del evento (hero) más alta y hacia abajo
        float heroY = 500;
        float heroH = 220;
        if (eventImageBytes != null) {
            try {
                java.io.InputStream in = new java.io.ByteArrayInputStream(eventImageBytes);
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    PDImageXObject eventImg = PDImageXObject.createFromByteArray(document, eventImageBytes, "eventimg");
                    // Altura aumentada a 220px, empieza más abajo para no tapar título
                    content.drawImage(eventImg, 0, heroY, 595, heroH);
                }
            } catch (Exception e) { /* ignora */ }
        }

        // ...el bloque correcto ya está más abajo, solo uno debe quedar...

        // QR y datos justo debajo del hero
        BufferedImage qr = generateQr(ticket.getQrContent());
        File tempQr = File.createTempFile("qr_", ".png");
        ImageIO.write(qr, "png", tempQr);
        PDImageXObject qrImg = PDImageXObject.createFromFile(tempQr.getAbsolutePath(), document);
        // QR a la izquierda, info a la derecha, ambos justo debajo del hero
        float qrX = 40;
        float qrY = heroY - 130; // justo debajo del hero
        content.drawImage(qrImg, qrX, qrY, 120, 120);
        tempQr.delete();

        // Info principal (en negrita los campos)
        float infoX = qrX + 140; // margen entre QR e info
        float infoY = qrY + 100;
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.newLineAtOffset(infoX, infoY);
        content.showText(ticket.getEventName() != null ? ticket.getEventName() : "");
        content.endText();

        infoY -= 20;
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(infoX, infoY);
        content.showText("Código: ");
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText(ticket.getCode());
        content.endText();

        infoY -= 18;
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(infoX, infoY);
        content.showText("Nombre: ");
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText(ticket.getNombre());
        content.endText();

        infoY -= 18;
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(infoX, infoY);
        content.showText("DNI: ");
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText(ticket.getDni());
        content.endText();

        infoY -= 18;
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(infoX, infoY);
        content.showText("Precio: ");
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText(ticket.getPrecio() + " €");
        content.endText();

        infoY -= 18;
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(infoX, infoY);
        content.showText("Butaca: ");
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText(idx + "/" + total);
        content.endText();

        // Detalles evento (solo 'Ubicación:' y 'Fecha:' en negrita, resto normal), esquina inferior izquierda
        float detailsX = 40;
        float detailsY = 60;
        // Ubicación
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(detailsX, detailsY);
        content.showText("Ubicación: ");
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText(ticket.getUbicacion() != null ? ticket.getUbicacion() : "");
        content.endText();
        // Fecha
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(detailsX, detailsY - 18);
        content.showText("Fecha: ");
        content.setFont(PDType1Font.HELVETICA, 12);
        content.showText(ticket.getFecha() != null ? ticket.getFecha() : "");
        content.endText();

        content.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return out;
    }

    private BufferedImage generateQr(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 200, 200, hints);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new RuntimeException("Error generando QR", e);
        }
    }
}
