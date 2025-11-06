// src/main/java/com/golive/backend/controller/AuthController.java
package com.golive.backend.controller;

import com.golive.backend.dto.AuthResponse;
import com.golive.backend.dto.LoginRequest;
import com.golive.backend.dto.RegisterRequest;
import com.golive.backend.model.User;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;

    // ========== ENDPOINTS DE AUTENTICACIÓN ==========
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            String token = authService.login(new LoginRequest(request.getEmail(), request.getPassword()));
            
            AuthResponse response = new AuthResponse(
                token, 
                user.getEmail(), 
                user.getName(), 
                user.getRole()
            );
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            AuthResponse response = new AuthResponse(
                token, 
                user.getEmail(), 
                user.getName(), 
                user.getRole()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== ENDPOINTS DE GESTIÓN DE USUARIOS ==========

    // GET: Obtener todos los usuarios (solo para SUPER_USER)
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            // Verificar que el usuario sea SUPER_USER
            if (!authService.isSuperUser(token)) {
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol SUPER_USER");
            }
            
            List<User> users = userService.findAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener usuarios: " + e.getMessage());
        }
    }

    // GET: Obtener usuario por ID
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id, @RequestHeader("Authorization") String token) {
        try {
            // Verificar permisos
            if (!authService.hasUserAccess(token, id)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }
            
            Optional<User> user = userService.findUserById(id);
            return user.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener usuario: " + e.getMessage());
        }
    }

    // POST: Crear nuevo usuario (solo SUPER_USER)
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest request, 
                                       @RequestHeader("Authorization") String token) {
        try {
            // Verificar que el usuario sea SUPER_USER
            if (!authService.isSuperUser(token)) {
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol SUPER_USER");
            }
            
            // Verificar si el email ya existe
            if (userService.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("El email ya está registrado");
            }
            
            User user = authService.register(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al crear usuario: " + e.getMessage());
        }
    }

    // PUT: Actualizar usuario existente
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, 
                                       @RequestBody User userDetails,
                                       @RequestHeader("Authorization") String token) {
        try {
            // Verificar permisos
            if (!authService.hasUserAccess(token, id)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }
            
            Optional<User> existingUser = userService.findUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = existingUser.get();
            
            // Actualizar campos permitidos
            if (userDetails.getName() != null) {
                user.setName(userDetails.getName());
            }
            if (userDetails.getEmail() != null) {
                // Verificar que el nuevo email no esté en uso
                Optional<User> userWithEmail = userService.findByEmail(userDetails.getEmail());
                if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body("El email ya está en uso");
                }
                user.setEmail(userDetails.getEmail());
            }
            if (userDetails.getRole() != null && authService.isSuperUser(token)) {
                // Solo SUPER_USER puede cambiar roles
                user.setRole(userDetails.getRole());
            }

            // ✅ CAMBIO: usa save() en lugar de saveUser()
            User updatedUser = userService.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    // PATCH: Actualizar solo el rol del usuario (solo SUPER_USER)
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String id, 
                                           @RequestBody String newRole,
                                           @RequestHeader("Authorization") String token) {
        try {
            // Verificar que el usuario sea SUPER_USER
            if (!authService.isSuperUser(token)) {
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol SUPER_USER");
            }
            
            Optional<User> existingUser = userService.findUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = existingUser.get();
            user.setRole(newRole);

            // ✅ CAMBIO: usa save() en lugar de saveUser()
            User updatedUser = userService.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar rol: " + e.getMessage());
        }
    }

    // DELETE: Eliminar usuario
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id, 
                                       @RequestHeader("Authorization") String token) {
        try {
            // Verificar permisos (solo SUPER_USER o el propio usuario)
            if (!authService.hasUserAccess(token, id) && !authService.isSuperUser(token)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }
            
            Optional<User> existingUser = userService.findUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    // GET: Estadísticas de usuarios (solo SUPER_USER)
    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats(@RequestHeader("Authorization") String token) {
        try {
            // Verificar que el usuario sea SUPER_USER
            if (!authService.isSuperUser(token)) {
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol SUPER_USER");
            }
            
            long totalUsers = userService.countAllUsers();
            long adminUsers = userService.countUsersByRole("admin");
            long superUsers = userService.countUsersByRole("super_user");
            long regularUsers = userService.countUsersByRole("user");
            
            UserStatsResponse stats = new UserStatsResponse(totalUsers, adminUsers, superUsers, regularUsers);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    // Endpoint de prueba
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("✅ Backend funcionando - " + java.time.LocalDateTime.now());
    }

    // Clase interna para respuesta de estadísticas
    public static class UserStatsResponse {
        private long totalUsers;
        private long adminUsers;
        private long superUsers;
        private long regularUsers;

        public UserStatsResponse(long totalUsers, long adminUsers, long superUsers, long regularUsers) {
            this.totalUsers = totalUsers;
            this.adminUsers = adminUsers;
            this.superUsers = superUsers;
            this.regularUsers = regularUsers;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getAdminUsers() { return adminUsers; }
        public long getSuperUsers() { return superUsers; }
        public long getRegularUsers() { return regularUsers; }
    }
}