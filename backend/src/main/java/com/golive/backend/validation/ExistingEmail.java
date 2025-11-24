package com.golive.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingEmailValidator.class)
@Documented
public @interface ExistingEmail {
    String message() default "No existe ningún usuario con ese correo";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
