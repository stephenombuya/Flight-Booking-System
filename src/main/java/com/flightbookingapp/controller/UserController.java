package com.flightbookingapp.controller;

import com.flightbookingapp.dto.request.ChangePasswordRequest;
import com.flightbookingapp.dto.request.UpdateUserRequest;
import com.flightbookingapp.dto.response.ApiResponse;
import com.flightbookingapp.dto.response.UserResponse;
import com.flightbookingapp.model.User;
import com.flightbookingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authenticated user profile management.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's profile")
    public ApiResponse<UserResponse> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(userService.getUserById(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID (admin or owner only)")
    public ApiResponse<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user's profile (admin or owner only)")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success("Profile updated", userService.updateUser(id, request, currentUser));
    }

    @PatchMapping("/{id}/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change a user's password (admin or owner only)")
    public void changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(id, request, currentUser);
    }
}
