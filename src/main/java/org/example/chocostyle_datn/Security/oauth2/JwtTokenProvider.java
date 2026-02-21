package org.example.chocostyle_datn.Security.oauth2;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final String JWT_SECRET =
            "54686973415365637265744b6579466f724a57545369676e696e67507572706f736573313233";

    private final long JWT_EXPIRATION = 86400000L; // 1 ngày

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 🔥 NEW: Lấy toàn bộ Claims ra 1 lần duy nhất để tối ưu hiệu năng
    public Claims getClaimsFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromJWT(String token) {
        return getClaimsFromJWT(token).getSubject();
    }

    public String getRoleFromJWT(String token) {
        return getClaimsFromJWT(token).get("role", String.class);
    }
}