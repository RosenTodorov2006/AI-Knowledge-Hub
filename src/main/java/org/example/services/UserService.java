package org.example.services;

import org.example.models.dtos.importDtos.RegisterSeedDto;

public interface UserService {
    void register(RegisterSeedDto registerSeedDto);
    boolean isValidEmail(String email);
    boolean isValidUsername(String username);
}
