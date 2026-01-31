package org.example.services.impl;

import jakarta.transaction.Transactional;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.ApplicationRole;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final String MSG_KEY_NOT_FOUND = "error.user.notfound";
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public UserServiceImpl(UserRepository userRepository,
                           ModelMapper modelMapper,
                           PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    @Override
    public void register(RegisterSeedDto registerSeedDto) {
        UserEntity user = this.modelMapper.map(registerSeedDto, UserEntity.class);

        user.setPassword(this.passwordEncoder.encode(registerSeedDto.getPassword()));

        user.setRole(this.userRepository.count() == 0 ? ApplicationRole.ADMIN : ApplicationRole.USER);

        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        this.userRepository.save(user);
    }

    @Override
    public boolean isEmailUnique(String email) {
        return this.userRepository.findByEmail(email).isEmpty();
    }

    @Override
    public boolean isUsernameUnique(String username) {
        return this.userRepository.findByUsername(username).isEmpty();
    }

    @Override
    @Transactional
    public void changeProfileInfo(ChangeProfileDto changeProfileDto, String email) {
        UserEntity userEntity = findByEmailOrThrow(email);
        userEntity.setEmail(changeProfileDto.getEmail());
        userEntity.setFullName(changeProfileDto.getFullName());
    }

    @Override
    @Transactional
    public void changeUserPassword(ChangeUserPasswordDto changeUserPasswordDto, String email) {
        UserEntity userEntity = findByEmailOrThrow(email);
        userEntity.setPassword(passwordEncoder.encode(changeUserPasswordDto.getPassword()));
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        UserEntity userEntity = findByEmailOrThrow(email);
        userEntity.setActive(false);
    }

    @Override
    public UserViewDto getUserViewByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .map(user -> this.modelMapper.map(user, UserViewDto.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage(MSG_KEY_NOT_FOUND, null, LocaleContextHolder.getLocale())));
    }

    private UserEntity findByEmailOrThrow(String email) {
        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage(MSG_KEY_NOT_FOUND, null, LocaleContextHolder.getLocale())));
    }

    @Override
    public ChangeProfileDto getChangeProfileDto(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage(MSG_KEY_NOT_FOUND, null, LocaleContextHolder.getLocale())));

        ChangeProfileDto dto = new ChangeProfileDto();
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        return dto;
    }

    @Override
    public UserEntity findUserByEmail(String gmail) {
        return userRepository.findByEmail(gmail)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage(MSG_KEY_NOT_FOUND, null, LocaleContextHolder.getLocale())));
    }
}
