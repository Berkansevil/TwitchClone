package com.berkan.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 6 , message = "New password must be at least 6 characters")
    private String newPassword;
}
