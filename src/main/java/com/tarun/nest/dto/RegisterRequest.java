package com.tarun.nest.dto;

import jakarta.validation.constraints.Email;
import com.tarun.nest.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		
		@NotBlank(message = "Name is required")
		String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,
        
        @NotBlank(message = "Phone number is required")
		@Size(min = 10, max = 10, message = "Phone number must be in 10 digits")
		String phoneNumber,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password,
        
        @NotBlank(message = "Confirm password is required")
		@Size(min = 8, message = "Confirm password must contain at least 8 characters")
		String confirmPassword,
        
		@NotNull(message = "Role is required")
		Role role
        

) {
	
}