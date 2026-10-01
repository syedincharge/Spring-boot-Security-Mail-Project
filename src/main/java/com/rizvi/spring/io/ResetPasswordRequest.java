package com.rizvi.spring.io;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {


    @NotBlank(message= "Email address is required")
    private String email;
    @NotBlank(message= "OTP is required")
    private String otp;
    @NotBlank(message = "New password is required")
    @JsonAlias({"new_password", "password"})   // accept alternates
    private String newPassword;

}
