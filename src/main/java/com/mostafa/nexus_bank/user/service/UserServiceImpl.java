package com.mostafa.nexus_bank.user.service;

import com.mostafa.nexus_bank.cache.config.CacheNames;
import com.mostafa.nexus_bank.common.event.PasswordChangedEvent;
import com.mostafa.nexus_bank.common.event.UserLockedEvent;
import com.mostafa.nexus_bank.common.event.UserRegisteredEvent;
import com.mostafa.nexus_bank.exception.DuplicateResourceException;
import com.mostafa.nexus_bank.exception.EntityNotFoundException;
import com.mostafa.nexus_bank.exception.ValidationException;
import com.mostafa.nexus_bank.user.dto.request.ChangePasswordRequest;
import com.mostafa.nexus_bank.user.dto.request.UpdateUserRequest;
import com.mostafa.nexus_bank.user.dto.response.UserPageResponse;
import com.mostafa.nexus_bank.user.dto.response.UserProfileResponse;
import com.mostafa.nexus_bank.user.dto.response.UserResponse;
import com.mostafa.nexus_bank.user.entity.User;
import com.mostafa.nexus_bank.user.mapper.UserMapper;
import com.mostafa.nexus_bank.user.repository.UserRepository;
import com.mostafa.nexus_bank.user.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USERS, key = "#email", unless = "#result == null")
    public UserProfileResponse getProfile(String email) {
        log.debug("Fetching profile for user: {}", email);

        User user = findUserByEmail(email);
        return userMapper.toProfileResponse(user);
    }

    @Override
    @CachePut(value = CacheNames.USERS, key = "#email")
    public UserProfileResponse updateProfile(String email, UpdateUserRequest request) {
        log.debug("Updating profile for user: {}", email);

        User user = findUserByEmail(email);

        if (request.phone() != null && !request.phone().equals(user.getPhone())) {
            checkDuplicatePhone(request.phone());
        }

        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);

        log.info("Profile updated for user: {}", email);
        return userMapper.toProfileResponse(updatedUser);
    }

    @Override
    @CacheEvict(value = CacheNames.USERS, key = "#email")
    public void changePassword(String email, ChangePasswordRequest request) {
        log.debug("Changing password for user: {}", email);

        validatePasswordMatch(request.newPassword(), request.confirmPassword());

        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect", java.util.Map.of("currentPassword", "Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        eventPublisher.publishEvent(new PasswordChangedEvent(user.getId(), user.getEmail()));

        log.info("Password changed for user: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USERS, key = "'id::' + #id", unless = "#result == null")
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);

        User user = findUserById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPageResponse getAllUsers(String search, Pageable pageable) {
        log.debug("Fetching all users with search: {}", search);

        Specification<User> spec = UserSpecification.withFilters(search);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> users = userPage.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        return UserPageResponse.builder()
                .content(users)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    @Override
    @CacheEvict(value = CacheNames.USERS, allEntries = true)
    public void deleteUser(UUID id) {
        log.debug("Deleting user: {}", id);

        User user = findUserById(id);
        userRepository.delete(user);

        log.info("User deleted: {}", id);
    }

    @Override
    @CacheEvict(value = CacheNames.USERS, key = "'id::' + #id")
    public UserResponse lockUser(UUID id) {
        log.debug("Locking user: {}", id);

        User user = findUserById(id);
        user.setAccountNonLocked(false);
        User updatedUser = userRepository.save(user);

        eventPublisher.publishEvent(new UserLockedEvent(user.getId(), user.getEmail()));

        log.info("User locked: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @CacheEvict(value = CacheNames.USERS, key = "'id::' + #id")
    public UserResponse unlockUser(UUID id) {
        log.debug("Unlocking user: {}", id);

        User user = findUserById(id);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        User updatedUser = userRepository.save(user);

        log.info("User unlocked: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @CacheEvict(value = CacheNames.USERS, key = "'id::' + #id")
    public UserResponse enableUser(UUID id) {
        log.debug("Enabling user: {}", id);

        User user = findUserById(id);
        user.setEnabled(true);
        User updatedUser = userRepository.save(user);

        log.info("User enabled: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @CacheEvict(value = CacheNames.USERS, key = "'id::' + #id")
    public UserResponse disableUser(UUID id) {
        log.debug("Disabling user: {}", id);

        User user = findUserById(id);
        user.setEnabled(false);
        User updatedUser = userRepository.save(user);

        log.info("User disabled: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", id));
    }

    private void checkDuplicatePhone(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("User", "phone", phone);
        }
    }

    private void validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match", java.util.Map.of("confirmPassword", "Passwords do not match"));
        }
    }
}
