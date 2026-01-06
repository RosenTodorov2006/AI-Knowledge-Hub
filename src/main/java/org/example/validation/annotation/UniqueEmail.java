package org.example.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.validation.validators.UniqueEmailValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email is already in use!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
