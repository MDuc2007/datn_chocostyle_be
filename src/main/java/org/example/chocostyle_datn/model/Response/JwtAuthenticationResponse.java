package org.example.chocostyle_datn.model.Response;


import lombok.Data;


@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private String role; // Nếu sau này có phân quyền


    public JwtAuthenticationResponse(String accessToken, String username, String email) {
        this.accessToken = accessToken;
        this.username = username;
        this.email = email;
    }
}

