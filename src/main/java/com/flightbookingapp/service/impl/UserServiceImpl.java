package com.flightbookingapp.service.impl;

import com.flightbookingapp.dto.request.ChangePasswordRequest;
import com.flightbookingapp.dto.request.UpdateUserRequest;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.dto.response.UserResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ResourceNotFoundException;
import com.flightbookingapp.exception.UnauthorizedException;
import com.flightbookingapp.model.Role;
import com.flightbookingapp.model.User;
import com.flightbookingapp.repository.UserRepository;
import com.flightbookingapp.service.UserService;
import com.flightbookingapp.util.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages user profile reads and updates with ownership checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request, User currentUser) {
        assertOwnerOrAdmin(id, currentUser);

        User user = findOrThrow(id);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.setLastName(request.getLastName());
        if (request.getPhone()     != null) user.setPhone(request.getPhone());

        User saved = userRepository.save(user);
        log.info("User {} updated profile", saved.getEmail());
        return toResponse(saved);
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request, User currentUser) {
        assertOwnerOrAdmin(id, currentUser);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        User user = findOrThrow(id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed password", user.getEmail());
    }

    @Override
    public void disableUser(Long id) {
        User user = findOrThrow(id);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Admin disabled user {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        return PageUtils.toPagedResponse(userRepository.findAll(pageable), this::toResponse);
    }

    // ---- Helpers ----

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /** Ensures the caller is either the resource owner or an admin. */
    private void assertOwnerOrAdmin(Long resourceOwnerId, User currentUser) {
        boolean isOwner = currentUser.getId().equals(resourceOwnerId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorised to modify this resource");
        }
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
