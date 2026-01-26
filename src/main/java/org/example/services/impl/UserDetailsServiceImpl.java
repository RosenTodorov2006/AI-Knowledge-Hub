package org.example.services.impl;

import org.example.models.entities.UserEntity;
import org.example.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ERR_INVALID_EMAIL = "Invalid email";

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.userRepository.findByEmail(email)
                .map(UserDetailsServiceImpl::mapToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(ERR_INVALID_EMAIL));
    }

    private static UserDetails mapToUserDetails(UserEntity user) {
        return User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(getAllRoles(user))
                .disabled(!user.isActive())
                .build();
    }

    private static List<SimpleGrantedAuthority> getAllRoles(UserEntity user) {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));
    }
}
