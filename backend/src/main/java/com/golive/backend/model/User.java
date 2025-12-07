// src/main/java/com/golive/backend/model/User.java
package com.golive.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Document(collection = "users")
public class User implements UserDetails {
    
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String name;
    private String lastName;
    private String password;
    private String role = "user";
    private boolean enabled = true;

    // Información personal adicional
    private String phoneNumber;
    private String dateOfBirth;
    private String postalCode;
    
    // Foto de perfil (almacenada en Base64)
    private String profilePhoto;

    // Preferencias de usuario
    private boolean pushNotificationsEnabled = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private List<Object> purchases = new ArrayList<>();
    
    // Eventos favoritos del usuario
    private List<String> favoriteEvents = new ArrayList<>();

    // ======= Constructores =======

    public User() {}

    public User(String id, String email, String name, String password, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.purchases = new ArrayList<>(); 
    }

    // ======= Getters y Setters =======

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public boolean isPushNotificationsEnabled() { return pushNotificationsEnabled; }
    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) { this.pushNotificationsEnabled = pushNotificationsEnabled; }

    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Object> getPurchases() { return purchases; }
    public void setPurchases(List<Object> purchases) { this.purchases = purchases; }
    
    public List<String> getFavoriteEvents() { return favoriteEvents; }
    public void setFavoriteEvents(List<String> favoriteEvents) { this.favoriteEvents = favoriteEvents; }

    // ======= Métodos UserDetails =======

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // ======= Campos para recuperación de contraseña =======
    private String resetPasswordToken;
    private Long resetPasswordExpiry;

    // ======= Getters y Setters para recuperación de contraseña =======
    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public Long getResetPasswordExpiry() {
        return resetPasswordExpiry;
    }

    public void setResetPasswordExpiry(Long resetPasswordExpiry) {
        this.resetPasswordExpiry = resetPasswordExpiry;
    }
}
