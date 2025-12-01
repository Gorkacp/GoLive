package com.golive.backend.services;

import com.golive.backend.model.PushSubscription;
import com.golive.backend.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushSubscriptionRepository subscriptionRepository;

    @Value("${app.push.vapid.public-key}")
    private String vapidPublicKey;

    @Value("${app.push.vapid.private-key}")
    private String vapidPrivateKey;

    @Value("${app.push.vapid.subject:mailto:admin@golive.com}")
    private String vapidSubject;

    private PushService pushService;

    private PushService getPushService() {
        if (pushService == null && vapidPublicKey != null && !vapidPublicKey.isEmpty() 
            && vapidPrivateKey != null && !vapidPrivateKey.isEmpty()) {
            try {
                // Agregar BouncyCastle provider si no está disponible
                if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                    Security.addProvider(new BouncyCastleProvider());
                }
                pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
            } catch (Exception e) {
                log.error("Error inicializando PushService: {}", e.getMessage());
            }
        }
        return pushService;
    }

    public PushSubscription saveOrUpdateSubscription(String userId,
                                                     String endpoint,
                                                     java.util.Map<String, String> keys,
                                                     Long expirationTime,
                                                     String userAgent) {
        try {
            PushSubscription subscription = subscriptionRepository.findByEndpoint(endpoint)
                    .orElseGet(PushSubscription::new);

            subscription.setUserId(userId);
            subscription.setEndpoint(endpoint);
            subscription.setP256dh(keys != null ? keys.get("p256dh") : null);
            subscription.setAuth(keys != null ? keys.get("auth") : null);
            if (subscription.getCreatedAt() == null) {
                subscription.setCreatedAt(java.time.LocalDateTime.now());
            }
            subscription.setUpdatedAt(java.time.LocalDateTime.now());

            return subscriptionRepository.save(subscription);
        } catch (Exception e) {
            log.error("Error guardando suscripción push: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteSubscription(String endpoint) {
        try {
            subscriptionRepository.findByEndpoint(endpoint)
                    .ifPresent(subscriptionRepository::delete);
        } catch (Exception e) {
            log.error("Error eliminando suscripción push: {}", e.getMessage());
        }
    }

    @Async
    public CompletableFuture<Void> sendNotificationToUser(String userId, String title, String body, String icon, String url) {
        // De momento ignoramos el icono personalizado y usamos el de la PWA;
        // dejamos el parámetro para compatibilidad con los llamadores.
        return sendNotificationToUser(userId, title, body, url);
    }

    @Async
    public CompletableFuture<Void> sendNotificationToUser(String userId, String title, String body, String url) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<PushSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
                if (subscriptions.isEmpty()) {
                    log.debug("No hay suscripciones push para el usuario: {}", userId);
                    return;
                }

                String payload = createNotificationPayload(title, body, url);
                PushService service = getPushService();
                
                if (service == null) {
                    log.warn("PushService no está configurado. Verifica las claves VAPID.");
                    return;
                }

                for (PushSubscription sub : subscriptions) {
                    try {
                        Subscription subscription = new Subscription(
                            sub.getEndpoint(),
                            new Subscription.Keys(sub.getP256dh(), sub.getAuth())
                        );
                        
                        Notification notification = new Notification(subscription, payload);
                        service.send(notification);
                        log.info("Notificación push enviada a usuario: {}", userId);
                    } catch (Exception e) {
                        log.error("Error enviando notificación push a suscripción {}: {}", 
                            sub.getId(), e.getMessage());
                        // Si la suscripción es inválida, eliminarla
                        if (e.getMessage() != null && 
                            (e.getMessage().contains("410") || e.getMessage().contains("expired"))) {
                            subscriptionRepository.delete(sub);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error enviando notificación push: {}", e.getMessage());
            }
        });
    }

    @Async
    public CompletableFuture<Void> sendNotificationToAll(String title, String body, String url) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<PushSubscription> allSubscriptions = subscriptionRepository.findAll();
                if (allSubscriptions.isEmpty()) {
                    log.debug("No hay suscripciones push registradas");
                    return;
                }

                String payload = createNotificationPayload(title, body, url);
                PushService service = getPushService();
                
                if (service == null) {
                    log.warn("PushService no está configurado. Verifica las claves VAPID.");
                    return;
                }

                for (PushSubscription sub : allSubscriptions) {
                    try {
                        Subscription subscription = new Subscription(
                            sub.getEndpoint(),
                            new Subscription.Keys(sub.getP256dh(), sub.getAuth())
                        );
                        
                        Notification notification = new Notification(subscription, payload);
                        service.send(notification);
                    } catch (Exception e) {
                        log.error("Error enviando notificación push a suscripción {}: {}", 
                            sub.getId(), e.getMessage());
                        // Si la suscripción es inválida, eliminarla
                        if (e.getMessage() != null && 
                            (e.getMessage().contains("410") || e.getMessage().contains("expired"))) {
                            subscriptionRepository.delete(sub);
                        }
                    }
                }
                log.info("Notificaciones push enviadas a {} suscripciones", allSubscriptions.size());
            } catch (Exception e) {
                log.error("Error enviando notificaciones push: {}", e.getMessage());
            }
        });
    }

    private String createNotificationPayload(String title, String body, String url) {
        return String.format(
            "{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/favicon_io/android-chrome-192x192.png\",\"badge\":\"/favicon_io/android-chrome-192x192.png\",\"data\":{\"url\":\"%s\"}}",
            escapeJson(title),
            escapeJson(body),
            url != null ? url : "/"
        );
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}


