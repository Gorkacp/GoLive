// src/main/java/com/golive/backend/service/AuthService.java
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

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public User register(RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        
        return userService.save(user);
    }

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

    public boolean isSuperUser(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            String userEmail = jwtService.getEmailFromToken(cleanToken);
            Optional<User> user = userService.findByEmail(userEmail);
            return user.isPresent() && "SUPER_USER".equals(user.get().getRole());
        } catch (Exception e) {
            return false;
        }
    }

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
                   "SUPER_USER".equals(currentUser.get().getRole());
        } catch (Exception e) {
            return false;
        }
    }
}