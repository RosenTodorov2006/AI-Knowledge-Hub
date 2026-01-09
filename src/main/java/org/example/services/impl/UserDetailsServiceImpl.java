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
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.userRepository.findByEmail(email)
                .map(UserDetailsServiceImpl::map)
                .orElseThrow(()->new UsernameNotFoundException("Invalid email"));
    }
    private static UserDetails map(UserEntity user) {
        return User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(getAllRoles(user))
                .disabled(!user.isActive())
                .build();
    }
    private static List<SimpleGrantedAuthority> getAllRoles(UserEntity user) {
        return List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));
    }
}
