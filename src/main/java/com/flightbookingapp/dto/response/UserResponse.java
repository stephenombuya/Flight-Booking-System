package com.flightbookingapp.dto.response;

import com.flightbookingapp.model.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Safe user representation — never exposes the hashed password. */
@Data
@Builder
public class UserResponse {
    private Long          id;
    private String        firstName;
    private String        lastName;
    private String        email;
    private String        phone;
    private Role          role;
    private boolean       enabled;
    private LocalDateTime createdAt;
}
