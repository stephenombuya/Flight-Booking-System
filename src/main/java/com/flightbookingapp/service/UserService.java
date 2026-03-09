package com.flightbookingapp.service;

import com.flightbookingapp.dto.request.ChangePasswordRequest;
import com.flightbookingapp.dto.request.UpdateUserRequest;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.dto.response.UserResponse;
import com.flightbookingapp.model.User;
import org.springframework.data.domain.Pageable;

/** Contract for user profile and account management. */
public interface UserService {
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request, User currentUser);
    void changePassword(Long id, ChangePasswordRequest request, User currentUser);
    void disableUser(Long id);
    PagedResponse<UserResponse> getAllUsers(Pageable pageable);
}
