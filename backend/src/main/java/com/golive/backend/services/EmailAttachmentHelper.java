package com.golive.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.StreamUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class EmailAttachmentHelper {

    @Value("${mail.from.address}")
    private String fromAddress;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Nuevo método para soportar imágenes inline
    public void sendEmailWithAttachments(String to, String subject, String body, List<InputStreamSource> attachments, String filename, List<InputStreamSource> inlineImages, List<String> cids) {
        try {
            Map<String, Object> payload = new HashMap<>();
            // From
            Map<String, String> fromMap = new HashMap<>();
            fromMap.put("email", fromAddress);
            fromMap.put("name", fromName);
            payload.put("from", fromMap);
            // To
            List<Map<String, String>> toList = new ArrayList<>();
            Map<String, String> toMap = new HashMap<>();
            toMap.put("email", to);
            toList.add(toMap);
            payload.put("personalizations", List.of(Map.of("to", toList)));
            // Subject
            payload.put("subject", subject);
            // Content (HTML)
            List<Map<String, String>> contentList = new ArrayList<>();
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("type", "text/html");
            contentMap.put("value", body);
            contentList.add(contentMap);
            payload.put("content", contentList);
            // Attachments (PDFs)
            List<Map<String, Object>> attachList = new ArrayList<>();
            int i = 1;
            for (InputStreamSource src : attachments) {
                byte[] bytes = StreamUtils.copyToByteArray(src.getInputStream());
                Map<String, Object> attach = new HashMap<>();
                attach.put("content", Base64.getEncoder().encodeToString(bytes));
                attach.put("type", "application/pdf");
                attach.put("filename", (attachments.size() > 1 ? ("entrada_" + i + ".pdf") : filename));
                attach.put("disposition", "attachment");
                attachList.add(attach);
                i++;
            }
            // Inline images (event image)
            if (inlineImages != null && cids != null && inlineImages.size() == cids.size()) {
                for (int j = 0; j < inlineImages.size(); j++) {
                    InputStreamSource img = inlineImages.get(j);
                    String cid = cids.get(j);
                    byte[] imgBytes = StreamUtils.copyToByteArray(img.getInputStream());
                    // Detect MIME type (simple)
                    String mime = "image/jpeg";
                    if (imgBytes.length > 4) {
                        if (imgBytes[0] == (byte)0x89 && imgBytes[1] == 0x50 && imgBytes[2] == 0x4E && imgBytes[3] == 0x47) mime = "image/png";
                        else if (imgBytes[0] == (byte)0x52 && imgBytes[1] == (byte)0x49 && imgBytes[2] == (byte)0x46 && imgBytes[3] == (byte)0x46) mime = "image/webp";
                    }
                    Map<String, Object> attach = new HashMap<>();
                    attach.put("content", Base64.getEncoder().encodeToString(imgBytes));
                    attach.put("type", mime);
                    attach.put("filename", cid + (mime.equals("image/png") ? ".png" : mime.equals("image/webp") ? ".webp" : ".jpg"));
                    attach.put("disposition", "inline");
                    attach.put("content_id", cid);
                    attachList.add(attach);
                }
            }
            payload.put("attachments", attachList);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SENDGRID_API_URL))
                    .header("Authorization", "Bearer " + sendGridApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Error SendGrid: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error enviando email con adjuntos: " + e.getMessage(), e);
        }
    }
}
