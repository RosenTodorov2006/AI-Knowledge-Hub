package org.example.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.validation.annotation.ValidPasswords;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ValidatePasswordValidator  implements ConstraintValidator<ValidPasswords, RegisterSeedDto> {
    private String message;
    @Override
    public void initialize(ValidPasswords constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(RegisterSeedDto registerSeedDto, ConstraintValidatorContext constraintValidatorContext) {
        String password = registerSeedDto.getPassword();
        String confirmPassword = registerSeedDto.getConfirmPassword();
        if(password == null || confirmPassword == null){
            return true;
        }
        boolean isValid = confirmPassword.equals(password);

        if(!isValid) {
            constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)
                    .buildConstraintViolationWithTemplate(this.message)
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }

        return isValid;
    }
}
