package com.rizvi.spring.service;

import com.rizvi.spring.io.ProfileRequest;
import com.rizvi.spring.io.ProfileResponse;

public interface ProfileService {

     ProfileResponse createProfile(ProfileRequest request);
     ProfileResponse getProfile(String email);
     void sendResetOtp(String email);
     void resetPassword(String email, String otp, String newPassword);
     void sendOtp(String email);
     void verifyOtp(String email, String otp);

}
