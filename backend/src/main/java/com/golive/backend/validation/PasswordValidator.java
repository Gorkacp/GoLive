package com.golive.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    @Override
    public void initialize(ValidPassword annotation) {
        // Inicialización
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            addConstraintViolation(context, "La contraseña no puede estar vacía");
            return false;
        }
        
        StringBuilder violations = new StringBuilder();
        boolean isValid = true;
        
        // Verificar longitud mínima
        if (value.length() < 8) {
            violations.append("Mínimo 8 caracteres. ");
            isValid = false;
        }
        
        // Verificar longitud máxima
        if (value.length() > 128) {
            violations.append("Máximo 128 caracteres. ");
            isValid = false;
        }
        
        // Verificar mayúsculas
        if (!value.matches(".*[A-Z].*")) {
            violations.append("Debe contener al menos una mayúscula. ");
            isValid = false;
        }
        
        // Verificar minúsculas
        if (!value.matches(".*[a-z].*")) {
            violations.append("Debe contener al menos una minúscula. ");
            isValid = false;
        }
        
        // Verificar números
        if (!value.matches(".*\\d.*")) {
            violations.append("Debe contener al menos un número. ");
            isValid = false;
        }
        
        // Verificar caracteres especiales
        if (!value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*")) {
            violations.append("Debe contener al menos un carácter especial (!@#$%^&* etc). ");
            isValid = false;
        }
        
        // Verificar espacios en blanco
        if (value.contains(" ")) {
            violations.append("No puede contener espacios en blanco. ");
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
