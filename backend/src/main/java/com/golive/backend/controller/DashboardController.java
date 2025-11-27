package com.golive.backend.controller;

import com.golive.backend.dto.dashboard.DashboardSummaryDto;
import com.golive.backend.model.User;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService,
                               AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(@RequestHeader(value = "Authorization", required = false) String token,
                                         @RequestParam(value = "userId", required = false) String userId) {
        try {
            var requesterOptional = authService.getUserFromToken(token);
            if (requesterOptional.isEmpty()) {
                return ResponseEntity.status(401).body("Usuario no autenticado");
            }

            User requester = requesterOptional.get();
            boolean isSuper = "super_user".equalsIgnoreCase(requester.getRole());
            boolean isAdmin = "admin".equalsIgnoreCase(requester.getRole());

            if (userId == null || userId.isBlank()) {
                userId = requester.getId();
            } else if (!userId.equals(requester.getId()) && !isSuper) {
                return ResponseEntity.status(403).body("No tienes permisos para consultar este dashboard");
            }

            boolean includeAll = isSuper && (userId == null || userId.equals(requester.getId()));

            DashboardSummaryDto summary = dashboardService.buildSummary(userId, includeAll || isSuper);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al generar el dashboard: " + e.getMessage());
        }
    }
}

