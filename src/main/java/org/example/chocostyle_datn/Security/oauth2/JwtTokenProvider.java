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


    // 1. Secret Key: Chìa khóa bí mật để ký và giải mã token.
    // Trong dự án thật, hãy để cái này trong file application.properties
    // Đây là key 512-bit mã hóa sẵn để demo (Bạn có thể đổi chuỗi khác)
    private final String JWT_SECRET = "546869734973415365637265744b6579466f724a57545369676e696e67507572706f736573313233";


    // 2. Thời gian hết hạn của Token (ví dụ: 1 ngày = 86400000 ms)
    private final long JWT_EXPIRATION = 86400000L;


    // Lấy key chuẩn HMAC-SHA
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }


    // --- A. TẠO TOKEN TỪ EMAIL (HOẶC USERNAME) ---
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);


        return Jwts.builder()
                .setSubject(email) // Lưu email vào subject của token
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    // --- B. LẤY EMAIL TỪ TOKEN ---
    public String getEmailFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }


    // --- C. KIỂM TRA TOKEN CÓ HỢP LỆ KHÔNG ---
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}

