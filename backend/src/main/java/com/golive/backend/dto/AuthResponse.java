// src/main/java/com/golive/backend/dto/AuthResponse.java
package com.golive.backend.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String email;
    private String name;
    private String lastName;
    private String role;
    private String phoneNumber;
    private String dateOfBirth;
    private String postalCode;
    private String profilePhoto;
    private boolean pushNotificationsEnabled;
    
    public AuthResponse(String token, String email, String name, String role) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public AuthResponse(String token, String id, String email, String name, String lastName, 
                       String role, String phoneNumber, String dateOfBirth, String postalCode) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.postalCode = postalCode;
    }

    public AuthResponse(String token, String id, String email, String name, String lastName, 
                       String role, String phoneNumber, String dateOfBirth, String postalCode, String profilePhoto, boolean pushNotificationsEnabled) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.postalCode = postalCode;
        this.profilePhoto = profilePhoto;
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }
}