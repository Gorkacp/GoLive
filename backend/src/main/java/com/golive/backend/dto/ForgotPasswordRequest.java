// src/main/java/com/golive/backend/dto/ForgotPasswordRequest.java
package com.golive.backend.dto;

import com.golive.backend.validation.ExistingEmail;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @ExistingEmail
    private String email;
}