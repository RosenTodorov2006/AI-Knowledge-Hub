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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterSeedDto registerSeedDto) {
        UserEntity currentUser = this.modelMapper.map(registerSeedDto, UserEntity.class);
        currentUser.setPassword(this.passwordEncoder.encode(registerSeedDto.getPassword()));
        if(this.userRepository.count()==0){
            currentUser.setRole(ApplicationRole.ADMIN);
        }else{
            currentUser.setRole(ApplicationRole.USER);
        }
        currentUser.setActive(true);
        currentUser.setCreatedAt(LocalDateTime.now());
        this.userRepository.save(currentUser);
    }

    @Override
    public boolean isValidEmail(String email) {
        return !this.userRepository.findByEmail(email).isEmpty();
    }

    @Override
    public boolean isValidUsername(String username) {
        return !this.userRepository.findByUsername(username).isEmpty();
    }

    @Override
    @Transactional
    public void changeProfileInfo(ChangeProfileDto changeProfileDto, String email) {
        Optional<UserEntity> optionalUser = this.userRepository.findByEmail(email);
        if(optionalUser.isEmpty()){
            throw new NullPointerException("USER NOT FOUND!");
        }
        UserEntity userEntity = optionalUser.get();
        userEntity.setEmail(changeProfileDto.getEmail());
        userEntity.setFullName(changeProfileDto.getFullName());
    }

    @Override
    @Transactional
    public void changeUserPassword(ChangeUserPasswordDto changeUserPasswordDto, String email) {
        Optional<UserEntity> optionalUser = this.userRepository.findByEmail(email);
        if(optionalUser.isEmpty()){
            throw new NullPointerException("USER NOT FOUND!");
        }
        UserEntity userEntity = optionalUser.get();
        userEntity.setPassword(passwordEncoder.encode(changeUserPasswordDto.getPassword()));
    }

    @Override
    @Transactional
    public void deleteUser (String email) {
        UserEntity userEntity = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new NullPointerException("USER NOT FOUND!"));
        userEntity.setActive(false);
    }

    @Override
    public UserViewDto getUserViewByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .map(user -> {
                    UserViewDto dto = new UserViewDto();
                    dto.setEmail(user.getEmail());
                    dto.setFullName(user.getFullName());
                    return dto;
                })
                .orElseThrow(() -> new NullPointerException("USER NOT FOUND!"));
    }
}
