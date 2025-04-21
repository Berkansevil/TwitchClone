package com.berkan.user_service.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required.")
    @Size(min = 3, max = 20, message = "Email must be between 3 and 20 characters.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters.")
    private String password;
}
