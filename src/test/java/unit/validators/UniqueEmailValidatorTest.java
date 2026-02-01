package unit.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.example.services.UserService;
import org.mockito.Mockito;
import org.example.validation.validators.UniqueEmailValidator;
import org.example.validation.annotation.UniqueEmail;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
public class UniqueEmailValidatorTest {
    private UserService userService;
    private UniqueEmailValidator validator;
    private ConstraintValidatorContext context;
    @BeforeEach
    public void setUp() {
        userService = Mockito.mock(UserService.class);
        validator = new UniqueEmailValidator(userService);
        context = Mockito.mock(ConstraintValidatorContext.class);

        UniqueEmail annotation = Mockito.mock(UniqueEmail.class);
        validator.initialize(annotation);
    }
    @Test
    public void testIsValidShouldReturnTrueWhenEmailIsUnique() {
        String email = "new@example.com";
        Mockito.when(userService.isEmailUnique(email)).thenReturn(true);

        boolean result = validator.isValid(email, context);

        Assertions.assertTrue(result);
        Mockito.verify(userService).isEmailUnique(email);
    }
    @Test
    public void testIsValidShouldReturnFalseWhenEmailExists() {
        String email = "existing@example.com";
        Mockito.when(userService.isEmailUnique(email)).thenReturn(false);

        boolean result = validator.isValid(email, context);

        Assertions.assertFalse(result);
        Mockito.verify(userService).isEmailUnique(email);
    }
}
