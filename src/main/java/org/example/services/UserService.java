package org.example.services;

import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;

public interface UserService {
    void register(RegisterSeedDto registerSeedDto);
    boolean isEmailUnique(String email);
    boolean isUsernameUnique(String username);

    void changeProfileInfo(ChangeProfileDto changeProfileDto, String email);

    void changeUserPassword(ChangeUserPasswordDto changeUserPasswordDto, String email);

    void deleteUser(String email);
    UserViewDto getUserViewByEmail(String email);
    ChangeProfileDto getChangeProfileDto(String email);
}
