//package org.example.unit.service;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.example.models.entities.enums.ApplicationRole;
//import org.mockito.Mock;
//import org.example.repositories.UserRepository;
//import org.modelmapper.ModelMapper;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.context.MessageSource;
//import org.example.services.impl.UserServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.example.models.dtos.importDtos.RegisterSeedDto;
//import org.example.models.entities.UserEntity;
//import org.mockito.ArgumentCaptor;
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.verify;
//import org.junit.jupiter.api.Assertions;
//import java.util.Optional;
//import org.example.models.dtos.importDtos.ChangeProfileDto;
//import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import org.springframework.web.server.ResponseStatusException;
//import org.example.models.dtos.exportDtos.UserViewDto;
//
//@ExtendWith(MockitoExtension.class)
//public class UserServiceImplTest {
//    private static final String TEST_EMAIL = "user@example.com";
//    private static final String NEW_EMAIL = "new@example.com";
//    private static final String TEST_PASSWORD = "password123";
//    private static final String ENCODED_PASSWORD = "encoded_password";
//    private static final String FULL_NAME = "John Doe";
//    private static final String ERR_MSG = "User not found";
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private ModelMapper modelMapper;
//    @Mock
//    private PasswordEncoder passwordEncoder;
//    @Mock
//    private MessageSource messageSource;
//
//    private UserServiceImpl userService;
//    @BeforeEach
//    public void setUp() {
//        userService = new UserServiceImpl(userRepository, modelMapper, passwordEncoder, messageSource);
//    }
//    @Test
//    public void testRegisterShouldSetAdminRoleWhenFirstUser() {
//        RegisterSeedDto dto = new RegisterSeedDto();
//        dto.setPassword(TEST_PASSWORD);
//        UserEntity user = new UserEntity();
//        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
//
//        when(modelMapper.map(dto, UserEntity.class)).thenReturn(user);
//        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
//        when(userRepository.count()).thenReturn(0L);
//
//        userService.register(dto);
//
//        verify(userRepository).save(userCaptor.capture());
//        UserEntity savedUser = userCaptor.getValue();
//        Assertions.assertEquals(ApplicationRole.ADMIN, savedUser.getRole());
//        Assertions.assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
//        Assertions.assertTrue(savedUser.isActive());
//    }
//    @Test
//    public void testRegisterShouldSetUserRoleWhenNotFirstUser() {
//        RegisterSeedDto dto = new RegisterSeedDto();
//        dto.setPassword(TEST_PASSWORD);
//        UserEntity user = new UserEntity();
//        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
//
//        when(modelMapper.map(dto, UserEntity.class)).thenReturn(user);
//        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
//        when(userRepository.count()).thenReturn(10L);
//
//        userService.register(dto);
//
//        verify(userRepository).save(userCaptor.capture());
//        Assertions.assertEquals(ApplicationRole.USER, userCaptor.getValue().getRole());
//    }
//    @Test
//    public void testIsEmailUniqueShouldReturnTrueWhenEmpty() {
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
//
//        boolean result = userService.isEmailUnique(TEST_EMAIL);
//
//        Assertions.assertTrue(result);
//    }
//    @Test
//    public void testIsUsernameUniqueShouldReturnTrueWhenEmpty() {
//        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
//
//        boolean result = userService.isUsernameUnique("uniqueUser");
//
//        Assertions.assertTrue(result);
//    }
//    @Test
//    public void testChangeProfileInfoShouldUpdateEntity() {
//        ChangeProfileDto dto = new ChangeProfileDto();
//        dto.setEmail(NEW_EMAIL);
//        dto.setFullName(FULL_NAME);
//        UserEntity user = new UserEntity();
//        user.setEmail(TEST_EMAIL);
//
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
//
//        userService.changeProfileInfo(dto, TEST_EMAIL);
//
//        Assertions.assertEquals(NEW_EMAIL, user.getEmail());
//        Assertions.assertEquals(FULL_NAME, user.getFullName());
//    }
//    @Test
//    public void testChangeUserPasswordShouldEncodeNewPassword() {
//        ChangeUserPasswordDto dto = new ChangeUserPasswordDto();
//        dto.setPassword(TEST_PASSWORD);
//        UserEntity user = new UserEntity();
//
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
//        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
//
//        userService.changeUserPassword(dto, TEST_EMAIL);
//
//        Assertions.assertEquals(ENCODED_PASSWORD, user.getPassword());
//    }
//    @Test
//    public void testDeleteUserShouldSetInactive() {
//        UserEntity user = new UserEntity();
//        user.setActive(true);
//
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
//
//        userService.deleteUser(TEST_EMAIL);
//
//        Assertions.assertFalse(user.isActive());
//    }
//    @Test
//    public void testGetUserViewByEmailShouldReturnMappedDto() {
//        UserEntity user = new UserEntity();
//        UserViewDto expectedDto = new UserViewDto();
//
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
//        when(modelMapper.map(user, UserViewDto.class)).thenReturn(expectedDto);
//
//        UserViewDto result = userService.getUserViewByEmail(TEST_EMAIL);
//
//        Assertions.assertEquals(expectedDto, result);
//    }
//    @Test
//    public void testGetUserViewByEmailShouldThrowWhenNotFound() {
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
//        when(messageSource.getMessage(any(), any(), any())).thenReturn(ERR_MSG);
//
//        Assertions.assertThrows(ResponseStatusException.class, () ->
//                userService.getUserViewByEmail(TEST_EMAIL));
//    }
//    @Test
//    public void testGetChangeProfileDtoShouldReturnPopulatedDto() {
//        UserEntity user = new UserEntity();
//        user.setEmail(TEST_EMAIL);
//        user.setFullName(FULL_NAME);
//
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
//
//        ChangeProfileDto result = userService.getChangeProfileDto(TEST_EMAIL);
//
//        Assertions.assertEquals(TEST_EMAIL, result.getEmail());
//        Assertions.assertEquals(FULL_NAME, result.getFullName());
//    }
//    @Test
//    public void testGetChangeProfileDtoShouldThrowWhenNotFound() {
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
//        when(messageSource.getMessage(anyString(), any(), any())).thenReturn(ERR_MSG);
//
//        Assertions.assertThrows(ResponseStatusException.class, () ->
//                userService.getChangeProfileDto(TEST_EMAIL));
//    }
//    @Test
//    public void testFindUserByEmailShouldReturnUserOnSuccess() {
//        UserEntity user = new UserEntity();
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
//
//        UserEntity result = userService.findUserByEmail(TEST_EMAIL);
//
//        Assertions.assertEquals(user, result);
//    }
//    @Test
//    public void testFindUserByEmailShouldThrowRuntimeExceptionWhenNotFound() {
//        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
//        when(messageSource.getMessage(anyString(), any(), any())).thenReturn(ERR_MSG);
//
//        Assertions.assertThrows(RuntimeException.class, () ->
//                userService.findUserByEmail(TEST_EMAIL));
//    }
//}