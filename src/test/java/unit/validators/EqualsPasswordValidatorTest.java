package unit.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.example.validation.validators.EqualsPasswordValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.example.validation.annotation.ValidPasswords;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.anyString;
import org.junit.jupiter.api.Assertions;
public class EqualsPasswordValidatorTest {
    private EqualsPasswordValidator validator;
    private ConstraintValidatorContext context;
    private HibernateConstraintValidatorContext hibernateContext;
    private HibernateConstraintViolationBuilder hibernateBuilder;
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;
    @BeforeEach
    public void setUp() {
        validator = new EqualsPasswordValidator();

        ValidPasswords annotation = Mockito.mock(ValidPasswords.class);
        Mockito.when(annotation.message()).thenReturn("Passwords must match");
        validator.initialize(annotation);

        context = Mockito.mock(ConstraintValidatorContext.class);
        hibernateContext = Mockito.mock(HibernateConstraintValidatorContext.class);
        hibernateBuilder = Mockito.mock(HibernateConstraintViolationBuilder.class);
        nodeBuilder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        Mockito.when(context.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(hibernateContext);
        Mockito.when(hibernateContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(hibernateBuilder);
        Mockito.when(hibernateBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        Mockito.when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }
    @Test
    public void testIsValidShouldReturnTrueWhenPasswordsMatch() {
        TestPasswordDto dto = new TestPasswordDto("password123", "password123");

        boolean result = validator.isValid(dto, context);

        Assertions.assertTrue(result);
    }
    @Test
    public void testIsValidShouldReturnFalseWhenPasswordsDoNotMatch() {
        TestPasswordDto dto = new TestPasswordDto("password123", "different");

        boolean result = validator.isValid(dto, context);

        Assertions.assertFalse(result);
        Mockito.verify(hibernateBuilder).addPropertyNode("confirmPassword");
        Mockito.verify(context).disableDefaultConstraintViolation();
    }
    @Test
    public void testIsValidShouldReturnFalseWhenPasswordIsNull() {
        TestPasswordDto dto = new TestPasswordDto(null, "confirm");

        boolean result = validator.isValid(dto, context);

        Assertions.assertFalse(result);
    }
    private static class TestPasswordDto {
        private final String password;
        private final String confirmPassword;

        public TestPasswordDto(String password, String confirmPassword) {
            this.password = password;
            this.confirmPassword = confirmPassword;
        }
        public String getPassword() {
            return password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }
    }
}
