package org.example.unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.models.entities.enums.ApplicationRole;
import org.mockito.Mock;
import org.example.repositories.UserRepository;
import org.springframework.context.MessageSource;
import org.example.services.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.models.entities.UserEntity;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.junit.jupiter.api.Assertions;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    private static final String TEST_EMAIL = "admin@knowledge.com";
    private static final String TEST_PASSWORD = "secret_password";
    private static final ApplicationRole TEST_ROLE = ApplicationRole.ADMIN;
    private static final String EXPECTED_ROLE = "ROLE_ADMIN";
    private static final String ERR_NOT_FOUND = "User not found";
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageSource messageSource;
    private UserDetailsServiceImpl userDetailsService;
    @BeforeEach
    public void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository, messageSource);
    }
    @Test
    public void testLoadUserByUsernameShouldMapCorrectUserDetails() {
        UserEntity user = new UserEntity();
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        user.setRole(TEST_ROLE);
        user.setActive(true);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername(TEST_EMAIL);

        Assertions.assertEquals(TEST_EMAIL, result.getUsername());
        Assertions.assertEquals(TEST_PASSWORD, result.getPassword());
        Assertions.assertTrue(result.isEnabled());
        Assertions.assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(EXPECTED_ROLE)));
    }
    @Test
    public void testLoadUserByUsernameShouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(messageSource.getMessage(any(), any(), any())).thenReturn(ERR_NOT_FOUND);

        Assertions.assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(TEST_EMAIL));
    }
    @Test
    public void testLoadUserByUsernameShouldReturnDisabledUserWhenInactive() {
        UserEntity user = new UserEntity();
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        user.setRole(TEST_ROLE);
        user.setActive(false);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername(TEST_EMAIL);

        Assertions.assertFalse(result.isEnabled());
    }
}
