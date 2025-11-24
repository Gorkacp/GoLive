package com.golive.backend.validation;

import com.golive.backend.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExistingEmailValidator implements ConstraintValidator<ExistingEmail, String> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void initialize(ExistingEmail annotation) {
        // Inicialización
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            log.warn("Email es nulo o vacío");
            addConstraintViolation(context, "El email no puede estar vacío");
            return false;
        }
        
        // Normalizar el email
        String normalizedEmail = value.trim().toLowerCase();
        
        // Validar que el email sea un formato válido
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            log.warn("Formato de email inválido: {}", normalizedEmail);
            addConstraintViolation(context, "El formato del email no es válido");
            return false;
        }
        
        // Verificar que el email exista en el sistema
        boolean exists = userRepository.existsByEmail(normalizedEmail);
        log.info("Validando email existente: {} - Existe: {}", normalizedEmail, exists);
        
        if (!exists) {
            addConstraintViolation(context, "No existe ningún usuario con ese correo");
            return false;
        }
        
        log.info("✅ Email validado exitosamente: {}", normalizedEmail);
        return true;
    }
    
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
