package com.golive.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String name;
    
    private String lastName;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
    
    private String phoneNumber;
    
    private String dateOfBirth;
    
    private String postalCode;
}