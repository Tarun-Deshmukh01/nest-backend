package com.tarun.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.tarun.nest.enums.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    
    private String message;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Boolean success;
}
