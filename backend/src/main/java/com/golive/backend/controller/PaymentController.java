package com.golive.backend.controller;

import com.golive.backend.dto.payment.PayPalCaptureRequest;
import com.golive.backend.dto.payment.PaymentResultResponse;
import com.golive.backend.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.frontend.url:http://localhost:3000}", allowCredentials = "true")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/paypal/capture")
    public ResponseEntity<?> capturePayPalPayment(@RequestBody PayPalCaptureRequest request,
                                                  @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Autenticación requerida"));
            }

            log.info("[Payments] Capturando pago PayPal orderId={} status={} userId={}",
                    request.getProviderOrderId(), request.getStatus(), request.getUserId());

            PaymentResultResponse response = paymentService.registerPayPalPayment(request, authorizationHeader);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("[Payments] Datos inválidos al registrar orderId={}: {}", request.getProviderOrderId(), ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("[Payments] Error inesperado al registrar orderId={}: {}", request.getProviderOrderId(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo registrar el pago", "details", ex.getMessage()));
        }
    }
}

