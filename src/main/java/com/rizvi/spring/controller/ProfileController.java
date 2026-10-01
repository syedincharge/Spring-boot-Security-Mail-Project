package com.rizvi.spring.controller;


import com.rizvi.spring.io.ProfileRequest;
import com.rizvi.spring.io.ProfileResponse;
import com.rizvi.spring.service.EmailService;
import com.rizvi.spring.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request){
        ProfileResponse response = profileService.createProfile(request);
          emailService.sendWelcomeEmail(response.getEmail(), response.getName());
            return response;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email){

        return profileService.getProfile(email);
    }
}
