package com.rizvi.spring.service;

import com.rizvi.spring.entity.UserEntity;
import com.rizvi.spring.io.ProfileRequest;
import com.rizvi.spring.io.ProfileResponse;
import com.rizvi.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ============================================================
    // CREATE
    // ============================================================
    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User email already exists");
        }

        UserEntity newProfile = convertToUserEntity(request);
        newProfile = userRepository.save(newProfile);

        // Optional: send welcome email (best-effort)
        try {
            emailService.sendWelcomeEmail(newProfile.getEmail(), newProfile.getName());
        } catch (Exception e) {
            // Don't fail registration if email fails
            // Log it instead
            System.err.println("Welcome email failed: " + e.getMessage());
        }

        return convertToProfileResponse(newProfile);
    }

    // ============================================================
    // READ
    // ============================================================
    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + email));
        return convertToProfileResponse(existingUser);
    }

    // ============================================================
    // RESET PASSWORD — step 1 (send OTP)
    // ============================================================
    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + email));

        // 1. Generate OTP FIRST
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // 2. Persist OTP + expiry BEFORE sending
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000); // 15 min
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpiredAt(expiryTime);
        userRepository.save(existingUser);

        // 3. Send the email LAST — with the actual OTP
        try {
            emailService.sendResetOtpEmail(
                    existingUser.getEmail(),
                    existingUser.getName(),
                    otp
            );
        } catch (Exception e) {
            throw new RuntimeException("Unable to send reset OTP email", e);
        }
    }

    // ============================================================
    // RESET PASSWORD — step 2 (validate OTP + update password)
    // ============================================================
    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + email));

        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new IllegalArgumentException("Reset OTP is not valid");
        }
        if (existingUser.getResetOtpExpiredAt() < System.currentTimeMillis()) {
            throw new IllegalArgumentException("Reset OTP has expired");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpiredAt(0L);
        userRepository.save(existingUser);
    }

    // ============================================================
    // VERIFY EMAIL — step 1 (send OTP)
    // ============================================================
    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + email));

        if (Boolean.TRUE.equals(existingUser.getIsAccountVerified())) {
            throw new IllegalArgumentException("Email already verified");
        }

        // 1. Generate OTP FIRST
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // 2. Persist BEFORE sending
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000); // 15 min
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpiredAt(expiryTime);
        userRepository.save(existingUser);

        // 3. Send the email LAST — with the actual OTP
        try {
            emailService.sendVerifyOtpEmail(
                    existingUser.getEmail(),
                    existingUser.getName(),
                    otp
            );
        } catch (Exception e) {
            throw new RuntimeException("Unable to send verification OTP email", e);
        }
    }

    // ============================================================
    // VERIFY EMAIL — step 2 (validate OTP)
    // ============================================================
    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + email));

        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        if (existingUser.getVerifyOtpExpiredAt() < System.currentTimeMillis()) {
            throw new IllegalArgumentException("OTP has expired");
        }

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpiredAt(0L);
        userRepository.save(existingUser);
    }

    // ============================================================
    // MAPPERS
    // ============================================================
    private ProfileResponse convertToProfileResponse(UserEntity user) {
        return ProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getUserId())
                .isAccountVerified(user.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpiredAt(0L)
                .verifyOtp(null)
                .verifyOtpExpiredAt(0L)
                .resetOtp(null)
                .build();
    }
}
