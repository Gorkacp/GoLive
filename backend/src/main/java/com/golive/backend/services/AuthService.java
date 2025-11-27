package com.golive.backend.services;

import com.golive.backend.dto.LoginRequest;
import com.golive.backend.dto.RegisterRequest;
import com.golive.backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService; // <-- Inyectamos EmailService

    // Registro
    public User register(RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = new User();
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPostalCode(request.getPostalCode());
        user.setRole("user");

        return userService.save(user);
    }

    // Login
    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return jwtService.generateToken(request.getEmail());
    }

    // Verificar SUPER_USER
    public boolean isSuperUser(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            String cleanToken = token.replace("Bearer ", "");
            String userEmail = jwtService.getEmailFromToken(cleanToken);
            Optional<User> user = userService.findByEmail(userEmail);
            return user.isPresent() && "super_user".equals(user.get().getRole());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAdminOrSuper(String token) {
        return getUserFromToken(token)
                .map(user -> "super_user".equals(user.getRole()) || "admin".equals(user.getRole()))
                .orElse(false);
    }

    // Verificar acceso a usuario
    public boolean hasUserAccess(String token, String userId) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            String userEmail = jwtService.getEmailFromToken(cleanToken);
            Optional<User> currentUser = userService.findByEmail(userEmail);
            Optional<User> targetUser = userService.findUserById(userId);

            if (currentUser.isEmpty() || targetUser.isEmpty()) {
                return false;
            }

            return currentUser.get().getId().equals(userId) ||
                    "super_user".equals(currentUser.get().getRole());
        } catch (Exception e) {
            return false;
        }
    }

    // Generar token de recuperación y enviar email
    public String generatePasswordResetToken(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar token aleatorio
        String token = UUID.randomUUID().toString();

        // Guardar token y expiración (15 min)
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiry(System.currentTimeMillis() + 15 * 60 * 1000);
        userService.save(user);

        // Enviar correo usando EmailService
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return token; // Devuelve token solo si quieres usarlo internamente
    }

    // Restablecer contraseña
    public void resetPassword(String token, String newPassword) {
        User user = userService.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (user.getResetPasswordExpiry() == null || user.getResetPasswordExpiry() < System.currentTimeMillis()) {
            throw new RuntimeException("Token expirado");
        }

        // Actualizar contraseña y limpiar token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userService.save(user);
    }

    // Cambiar contraseña (usuario autenticado)
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("Debe poner otra contraseña distinta a la anterior");
        }

        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
    }

    // Eliminar cuenta (valida contraseña)
    public void deleteAccount(String userId, String password) {
        User user = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar contraseña
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("La contraseña es incorrecta. No se puede eliminar la cuenta.");
        }

        // Eliminar cuenta
        userService.deleteUser(userId);
    }

    // Obtener email desde el token (para uso en controladores)
    public String getEmailFromToken(String token) {
        return jwtService.getEmailFromToken(token);
    }

    public Optional<User> getUserFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            String cleanToken = token.replace("Bearer ", "").trim();
            if (cleanToken.isEmpty()) {
                return Optional.empty();
            }
            String userEmail = jwtService.getEmailFromToken(cleanToken);
            return userService.findByEmail(userEmail);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean canManageEvents(String token, String ownerId) {
        return getUserFromToken(token)
                .map(user -> {
                    if ("super_user".equals(user.getRole())) {
                        return true;
                    }
                    if ("admin".equals(user.getRole())) {
                        return ownerId == null || user.getId().equals(ownerId);
                    }
                    return ownerId != null && ownerId.equals(user.getId());
                })
                .orElse(false);
    }
}
