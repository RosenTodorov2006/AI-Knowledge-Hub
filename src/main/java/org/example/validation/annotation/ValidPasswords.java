package org.example.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.validation.validators.EqualsPasswordValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = EqualsPasswordValidator.class)
public @interface ValidPasswords {
    String message() default "{validation.user.passwords.match.equals}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
