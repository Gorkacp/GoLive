package com.golive.backend.dto;

import com.golive.backend.validation.ValidPassword;
import com.golive.backend.validation.ValidName;
import com.golive.backend.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "El nombre es requerido")
    @ValidName
    private String name;
    
    private String lastName;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    @UniqueEmail
    private String email;
    
    @NotBlank(message = "La contraseña es requerida")
    @ValidPassword
    private String password;
    
    private String phoneNumber;
    
    private String dateOfBirth;
    
    private String postalCode;
}