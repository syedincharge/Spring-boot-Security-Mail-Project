package com.rizvi.spring.controller;

import com.rizvi.spring.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DebugController {

    private final JwtUtil jwtUtil;

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @GetMapping("/debug/token")
    public Map<String, Object> debugToken(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Map.of(
            "subject", claims.getSubject(),
            "issuer",  claims.getIssuer(),
            "expiration", claims.getExpiration(),
            "extractedEmail", jwtUtil.extractEmail(token)
        );
    }
}