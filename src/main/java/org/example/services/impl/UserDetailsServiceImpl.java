package org.example.services.impl;

import org.example.models.entities.UserEntity;
import org.example.repositories.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private static final String MSG_KEY_NOT_FOUND = "error.auth.user.notfound";
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public UserDetailsServiceImpl(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.userRepository.findByEmail(email)
                .map(UserDetailsServiceImpl::mapToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSource.getMessage(MSG_KEY_NOT_FOUND, null, LocaleContextHolder.getLocale())
                ));
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
