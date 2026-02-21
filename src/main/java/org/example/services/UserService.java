package org.example.services;

import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.models.entities.UserEntity;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    void register(RegisterSeedDto registerSeedDto);
    boolean isEmailUnique(String email);
    boolean isUsernameUnique(String username);

    boolean changeProfileInfo(ChangeProfileDto changeProfileDto, String email);

    boolean changeUserPassword(ChangeUserPasswordDto changeUserPasswordDto, String email);
    UserViewDto getUserViewByEmail(String email);
    ChangeProfileDto getChangeProfileDto(String email);
    UserEntity findUserByEmail(String gmail);
    List<UserEntity> findAllUsers();
    boolean verifyUser(String token);
    boolean deleteUser(String email, String password);
    boolean reactivateAccount(String email, String password);
    void deactivateInactiveUsers(int months);
    long countAllUsers();
    void toggleEmailNotifications(String email);
    void resendVerificationEmail(String email);
}
