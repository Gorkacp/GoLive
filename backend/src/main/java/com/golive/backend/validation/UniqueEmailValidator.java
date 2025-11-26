package com.golive.backend.validation;

import com.golive.backend.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void initialize(UniqueEmail annotation) {
        // Inicialización
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // La validación de no nulo se maneja con @NotNull o @NotBlank
        }
        
        // Normalizar el email
        String normalizedEmail = value.trim().toLowerCase();
        
        // Validar que el email sea un formato válido
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            addConstraintViolation(context, "El formato del email no es válido");
            return false;
        }
        
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(normalizedEmail)) {
            addConstraintViolation(context, "Este email ya está registrado en el sistema");
            return false;
        }
        
        return true;
    }
    
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
