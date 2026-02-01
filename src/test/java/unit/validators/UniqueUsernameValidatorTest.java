package unit.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.example.services.UserService;
import org.mockito.Mockito;
import org.example.validation.validators.UniqueUsernameValidator;
import org.example.validation.annotation.UniqueUsername;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
public class UniqueUsernameValidatorTest {
    private UserService userService;
    private UniqueUsernameValidator validator;
    private ConstraintValidatorContext context;
    @BeforeEach
    public void setUp() {
        userService = Mockito.mock(UserService.class);
        validator = new UniqueUsernameValidator(userService);
        context = Mockito.mock(ConstraintValidatorContext.class);

        UniqueUsername annotation = Mockito.mock(UniqueUsername.class);
        validator.initialize(annotation);
    }
    @Test
    public void testIsValidShouldReturnTrueWhenUsernameIsNull() {
        boolean result = validator.isValid(null, context);

        Assertions.assertTrue(result);
        Mockito.verifyNoInteractions(userService);
    }
    @Test
    public void testIsValidShouldReturnTrueWhenUsernameIsEmpty() {
        boolean result = validator.isValid("", context);

        Assertions.assertTrue(result);
        Mockito.verifyNoInteractions(userService);
    }
    @Test
    public void testIsValidShouldReturnTrueWhenUsernameIsUnique() {
        String username = "uniqueUser";
        Mockito.when(userService.isUsernameUnique(username)).thenReturn(true);

        boolean result = validator.isValid(username, context);

        Assertions.assertTrue(result);
        Mockito.verify(userService).isUsernameUnique(username);
    }
    @Test
    public void testIsValidShouldReturnFalseWhenUsernameExists() {
        String username = "existingUser";
        Mockito.when(userService.isUsernameUnique(username)).thenReturn(false);

        boolean result = validator.isValid(username, context);

        Assertions.assertFalse(result);
        Mockito.verify(userService).isUsernameUnique(username);
    }
}
