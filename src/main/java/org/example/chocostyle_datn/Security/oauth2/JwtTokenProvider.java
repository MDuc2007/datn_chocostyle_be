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


    // 🔐 SECRET KEY (đưa vào application.properties trong production)
    private final String JWT_SECRET =
            "546869734973415365637265744b6579466f724a57545369676e696e67507572706f736573313233";


    // ⏳ 1 ngày
    private final long JWT_EXPIRATION = 86400000L;


    // =========================================================
    // LẤY SIGNING KEY
    // =========================================================
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }


    // =========================================================
    // A. TẠO TOKEN (EMAIL + ROLE)
    // =========================================================
    public String generateToken(String email, String role) {


        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);


        return Jwts.builder()
                .setSubject(email)          // Lưu email
                .claim("role", role)        // ⭐ Lưu role vào claim
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    // =========================================================
    // B. LẤY EMAIL TỪ TOKEN
    // =========================================================
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();


        return claims.getSubject(); // username lưu trong subject
    }




    // =========================================================
    // C. LẤY ROLE TỪ TOKEN
    // =========================================================
    public String getRoleFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();


        return claims.get("role", String.class);
    }


    // =========================================================
    // D. KIỂM TRA TOKEN HỢP LỆ
    // =========================================================
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);


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

