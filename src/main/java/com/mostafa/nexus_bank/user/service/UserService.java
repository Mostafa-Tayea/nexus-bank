package com.mostafa.nexus_bank.user.service;

import com.mostafa.nexus_bank.user.dto.request.ChangePasswordRequest;
import com.mostafa.nexus_bank.user.dto.request.UpdateUserRequest;
import com.mostafa.nexus_bank.user.dto.response.UserPageResponse;
import com.mostafa.nexus_bank.user.dto.response.UserProfileResponse;
import com.mostafa.nexus_bank.user.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserProfileResponse getProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateUserRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    UserResponse getUserById(UUID id);

    UserPageResponse getAllUsers(String search, Pageable pageable);

    void deleteUser(UUID id);

    UserResponse lockUser(UUID id);

    UserResponse unlockUser(UUID id);

    UserResponse enableUser(UUID id);

    UserResponse disableUser(UUID id);
}
