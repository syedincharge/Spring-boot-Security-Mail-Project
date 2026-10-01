package com.rizvi.spring.io;


import lombok.*;

@AllArgsConstructor
@Getter
public class AuthResponse {
    private String email;
    private String token;
}
