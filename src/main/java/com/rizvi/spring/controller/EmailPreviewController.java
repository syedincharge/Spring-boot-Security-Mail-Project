package com.rizvi.spring.controller;

import com.rizvi.spring.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailPreviewController {

    private final EmailService emailService;

    @PostMapping("/dev/preview/verify-email")
    public void previewVerify(@RequestParam String to) {
        emailService.sendVerifyOtpEmail(to, "Test User", "123456");
    }

    @PostMapping("/dev/preview/reset-email")
    public void previewReset(@RequestParam String to) {
        emailService.sendResetOtpEmail(to, "Test User", "654321");
    }
}