package com.golive.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NameValidator.class)
@Documented
public @interface ValidName {
    String message() default "El nombre no es válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
