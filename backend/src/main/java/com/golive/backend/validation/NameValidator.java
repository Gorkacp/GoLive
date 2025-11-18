package com.golive.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NameValidator implements ConstraintValidator<ValidName, String> {
    
    @Override
    public void initialize(ValidName annotation) {
        // Inicialización
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            addConstraintViolation(context, "El nombre no puede estar vacío");
            return false;
        }
        
        String trimmed = value.trim();
        StringBuilder violations = new StringBuilder();
        boolean isValid = true;
        
        // Verificar longitud
        if (trimmed.length() < 2) {
            violations.append("El nombre debe tener mínimo 2 caracteres. ");
            isValid = false;
        }
        
        if (trimmed.length() > 100) {
            violations.append("El nombre debe tener máximo 100 caracteres. ");
            isValid = false;
        }
        
        // Verificar que contiene solo letras, espacios y algunos caracteres permitidos
        if (!trimmed.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s\\-']+")) {
            violations.append("El nombre solo puede contener letras, espacios, guiones y apóstrofes. ");
            isValid = false;
        }
        
        // Verificar que no comience o termine con espacios
        if (!value.equals(trimmed)) {
            violations.append("El nombre no puede comenzar o terminar con espacios. ");
            isValid = false;
        }
        
        // Verificar que no haya múltiples espacios consecutivos
        if (trimmed.contains("  ")) {
            violations.append("No puede haber múltiples espacios consecutivos. ");
            isValid = false;
        }
        
        // Verificar que no haya números
        if (trimmed.matches(".*\\d.*")) {
            violations.append("El nombre no puede contener números. ");
            isValid = false;
        }
        
        if (!isValid) {
            addConstraintViolation(context, violations.toString().trim());
        }
        
        return isValid;
    }
    
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
