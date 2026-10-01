package com.rizvi.spring.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileRequest {
    @NotBlank(message = "Name should not be empty")
    private String name;
    @Email(message= "Enter a valid email address")
    @NotNull(message = "Email should not be empty")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
