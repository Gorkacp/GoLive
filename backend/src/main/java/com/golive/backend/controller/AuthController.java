// src/main/java/com/golive/backend/controller/AuthController.java
package com.golive.backend.controller;

import com.golive.backend.dto.AuthResponse;
import com.golive.backend.dto.LoginRequest;
import com.golive.backend.dto.RegisterRequest;
import com.golive.backend.dto.RoleChangeRequest;
import com.golive.backend.model.User;
import com.golive.backend.services.AuthService;
import com.golive.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.golive.backend.dto.ForgotPasswordRequest;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Base64;

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
                user.getId(),
                user.getEmail(), 
                user.getName(),
                user.getLastName(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getPostalCode(),
                user.getProfilePhoto(),
                user.isPushNotificationsEnabled()
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
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getLastName(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getPostalCode(),
                user.getProfilePhoto(),
                user.isPushNotificationsEnabled()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Validar disponibilidad de email
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("available", false, "message", "Email es requerido"));
            }
            
            Optional<User> existingUser = userService.findByEmail(email.trim().toLowerCase());
            
            if (existingUser.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "available", false, 
                    "message", "Este email ya está registrado"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "available", true,
                    "message", "Email disponible"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "available", false,
                "message", "Error al validar email: " + e.getMessage()
            ));
        }
    }

    // Validar contraseña contra políticas de seguridad
    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody Map<String, String> body) {
        try {
            String password = body.get("password");
            
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false, 
                    "message", "La contraseña es requerida"
                ));
            }
            
            StringBuilder violations = new StringBuilder();
            boolean isValid = true;
            
            // Validaciones profesionales
            if (password.length() < 8) {
                violations.append("Mínimo 8 caracteres. ");
                isValid = false;
            }
            if (password.length() > 128) {
                violations.append("Máximo 128 caracteres. ");
                isValid = false;
            }
            if (!password.matches(".*[A-Z].*")) {
                violations.append("Debe contener al menos una mayúscula. ");
                isValid = false;
            }
            if (!password.matches(".*[a-z].*")) {
                violations.append("Debe contener al menos una minúscula. ");
                isValid = false;
            }
            if (!password.matches(".*\\d.*")) {
                violations.append("Debe contener al menos un número. ");
                isValid = false;
            }
            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*")) {
                violations.append("Debe contener al menos un carácter especial. ");
                isValid = false;
            }
            if (password.contains(" ")) {
                violations.append("No puede contener espacios en blanco. ");
                isValid = false;
            }
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Contraseña válida" : violations.toString().trim());
            response.put("requirements", Map.of(
                "length", password.length() >= 8 && password.length() <= 128,
                "uppercase", password.matches(".*[A-Z].*"),
                "lowercase", password.matches(".*[a-z].*"),
                "numbers", password.matches(".*\\d.*"),
                "specialCharacters", password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*"),
                "noSpaces", !password.contains(" ")
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "valid", false,
                "message", "Error al validar contraseña: " + e.getMessage()
            ));
        }
    }

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

    // GET: Obtener usuario actual (autenticado)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // Verificar que haya token
            if (token == null || token.trim().isEmpty()) {
                System.err.println("Token no proporcionado en header Authorization");
                return ResponseEntity.status(401).body(Map.of("error", "Token requerido"));
            }

            // Extraer email del token
            String cleanToken = token.replace("Bearer ", "").trim();
            
            if (cleanToken.isEmpty()) {
                System.err.println("Token vacío después de limpiar");
                return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
            }
            
            String userEmail = authService.getEmailFromToken(cleanToken);
            
            // Buscar usuario por email
            Optional<User> user = userService.findByEmail(userEmail);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                System.err.println("Usuario no encontrado para email: " + userEmail);
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }
        } catch (Exception e) {
            System.err.println("Error en getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido o expirado", "details", e.getMessage()));
        }
    }

    // PATCH: Preferencias de notificaciones push
    @PatchMapping("/users/{id}/preferences")
    public ResponseEntity<?> updateUserPreferences(@PathVariable String id,
                                                  @RequestBody Map<String, Object> body,
                                                  @RequestHeader("Authorization") String token) {
        try {
            if (!authService.hasUserAccess(token, id)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }

            Optional<User> existingUser = userService.findUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = existingUser.get();

            if (body.containsKey("pushNotificationsEnabled")) {
                Object value = body.get("pushNotificationsEnabled");
                if (value instanceof Boolean) {
                    user.setPushNotificationsEnabled((Boolean) value);
                }
            }

            User updatedUser = userService.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar preferencias: " + e.getMessage());
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
            if (userDetails.getLastName() != null) {
                user.setLastName(userDetails.getLastName());
            }
            if (userDetails.getEmail() != null) {
                // Verificar que el nuevo email no esté en uso
                Optional<User> userWithEmail = userService.findByEmail(userDetails.getEmail());
                if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body("El email ya está en uso");
                }
                user.setEmail(userDetails.getEmail());
            }
            if (userDetails.getPhoneNumber() != null) {
                user.setPhoneNumber(userDetails.getPhoneNumber());
            }
            if (userDetails.getDateOfBirth() != null) {
                user.setDateOfBirth(userDetails.getDateOfBirth());
            }
            if (userDetails.getPostalCode() != null) {
                user.setPostalCode(userDetails.getPostalCode());
            }
            if (userDetails.getRole() != null && authService.isSuperUser(token)) {
                // Solo SUPER_USER puede cambiar roles
                user.setRole(userDetails.getRole());
            }

            User updatedUser = userService.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    // POST: Subir foto de perfil
    @PostMapping("/users/{id}/profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(@PathVariable String id,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestHeader("Authorization") String token) {
        try {
            // Verificar permisos
            if (!authService.hasUserAccess(token, id)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }

            // Validar que sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("El archivo debe ser una imagen");
            }

            // Validar tamaño máximo (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("La imagen no puede pesar más de 5MB");
            }

            Optional<User> existingUser = userService.findUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Convertir la imagen a Base64
            String base64Photo = "data:" + contentType + ";base64," + 
                                Base64.getEncoder().encodeToString(file.getBytes());

            User user = existingUser.get();
            user.setProfilePhoto(base64Photo);
            User updatedUser = userService.save(user);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al subir la foto: " + e.getMessage());
        }
    }

    // DELETE: Eliminar foto de perfil
    @DeleteMapping("/users/{id}/profile-photo")
    public ResponseEntity<?> deleteProfilePhoto(@PathVariable String id,
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
            user.setProfilePhoto(null);
            User updatedUser = userService.save(user);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar la foto: " + e.getMessage());
        }
    }

    // PATCH: Actualizar solo el rol del usuario (solo SUPER_USER)
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String id, 
                                           @RequestBody RoleChangeRequest roleChangeRequest,
                                           @RequestHeader("Authorization") String token) {
        try {
            if (!authService.isSuperUser(token)) {
                return ResponseEntity.status(403).body("Acceso denegado: Se requiere rol SUPER_USER");
            }
            
            Optional<User> existingUser = userService.findUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = existingUser.get();
            user.setRole(roleChangeRequest.getRole());

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

    // DELETE: Eliminar cuenta con validación de contraseña
    @DeleteMapping("/users/{id}/account")
    public ResponseEntity<?> deleteAccount(@PathVariable String id,
                                          @RequestBody Map<String, String> body,
                                          @RequestHeader("Authorization") String token) {
        try {
            // Verificar permisos (solo el usuario puede eliminar su propia cuenta)
            if (!authService.hasUserAccess(token, id)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }

            // Validar campo de contraseña
            if (!body.containsKey("password") || body.get("password") == null || body.get("password").trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña es requerida");
            }

            String password = body.get("password").trim();

            // Eliminar cuenta con validación de contraseña
            authService.deleteAccount(id, password);

            return ResponseEntity.ok("Cuenta eliminada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar la cuenta: " + e.getMessage());
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

    // src/main/java/com/golive/backend/controller/AuthController.java
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // ✅ El email ya fue validado por @ExistingEmail
            String token = authService.generatePasswordResetToken(request.getEmail().trim());

            System.out.println("✅ Token de recuperación generado: " + token);

            return ResponseEntity.ok("Se ha enviado un enlace de recuperación a tu correo.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al procesar la recuperación: " + e.getMessage());
        }
    }

    // POST: Restablecer contraseña usando token
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestBody Map<String, String> body) {
        try {
            if (!body.containsKey("password")) {
                return ResponseEntity.badRequest().body("Falta el campo password");
            }

            String newPassword = body.get("password");

            // ✅ Usa directamente el método de AuthService
            authService.resetPassword(token, newPassword);

            return ResponseEntity.ok("Contraseña restablecida correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al restablecer la contraseña: " + e.getMessage());
        }
    }

    // POST: Cambiar contraseña (usuario autenticado)
    @PostMapping("/users/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable String id,
                                           @RequestBody Map<String, String> body,
                                           @RequestHeader("Authorization") String token) {
        try {
            // Verificar permisos
            if (!authService.hasUserAccess(token, id)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }

            // Validar campos
            if (!body.containsKey("currentPassword") || !body.containsKey("newPassword") || !body.containsKey("confirmPassword")) {
                return ResponseEntity.badRequest().body("Faltan campos requeridos");
            }

            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");
            String confirmPassword = body.get("confirmPassword");

            // Validar que las contraseñas coincidan
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("Las contraseñas no coinciden");
            }

            // Validar longitud mínima
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
            }

            // Cambiar contraseña
            authService.changePassword(id, currentPassword, newPassword);

            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

}