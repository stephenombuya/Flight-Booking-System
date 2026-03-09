package com.flightbookingapp.service;

import com.flightbookingapp.TestDataFactory;
import com.flightbookingapp.dto.request.ChangePasswordRequest;
import com.flightbookingapp.dto.request.UpdateUserRequest;
import com.flightbookingapp.dto.response.UserResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ResourceNotFoundException;
import com.flightbookingapp.exception.UnauthorizedException;
import com.flightbookingapp.model.User;
import com.flightbookingapp.repository.UserRepository;
import com.flightbookingapp.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    private User customer;
    private User admin;

    @BeforeEach
    void setUp() {
        customer = TestDataFactory.buildCustomer();
        admin    = TestDataFactory.buildAdmin();
    }

    @Test
    @DisplayName("getUserById returns UserResponse for existing user")
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        UserResponse response = userService.getUserById(1L);
        assertThat(response.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    @DisplayName("getUserById throws ResourceNotFoundException for missing user")
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateUser allows owner to update their own profile")
    void updateUser_ownerCanUpdate() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Janet");

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, request, customer);
        assertThat(response.getFirstName()).isEqualTo("Janet");
    }

    @Test
    @DisplayName("updateUser throws UnauthorizedException when different customer tries to update")
    void updateUser_differentUserForbidden() {
        User other = User.builder().id(99L)
                .role(com.flightbookingapp.model.Role.CUSTOMER).build();
        assertThatThrownBy(() -> userService.updateUser(1L, new UpdateUserRequest(), other))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("updateUser allows admin to update any user")
    void updateUser_adminCanUpdate() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPhone("+9999999999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> userService.updateUser(1L, request, admin))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("changePassword throws BadRequestException for wrong current password")
    void changePassword_wrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("WrongPass1");
        request.setNewPassword("NewPass123");
        request.setConfirmPassword("NewPass123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("WrongPass1", customer.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, request, customer))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password");
    }

    @Test
    @DisplayName("changePassword throws BadRequestException when passwords don't match")
    void changePassword_mismatch() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Password1");
        request.setNewPassword("NewPass123");
        request.setConfirmPassword("DifferentPass1");

        assertThatThrownBy(() -> userService.changePassword(1L, request, customer))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    @DisplayName("disableUser sets enabled=false")
    void disableUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.disableUser(1L);

        verify(userRepository).save(argThat(u -> !u.isEnabled()));
    }
}
