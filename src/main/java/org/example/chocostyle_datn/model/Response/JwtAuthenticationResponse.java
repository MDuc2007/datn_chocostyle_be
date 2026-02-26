package org.example.chocostyle_datn.model.Response;

import lombok.Data;

@Data
public class JwtAuthenticationResponse {

    private Integer id; // 👉 1. Thêm trường id vào đây (Dùng Integer hoặc Long tuỳ kiểu dữ liệu trong DB của bạn)
    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private String role;

    // 👉 2. Cập nhật lại Constructor để nhận thêm id (và role)
    public JwtAuthenticationResponse(Integer id, String accessToken, String username, String email, String role) {
        this.id = id;
        this.accessToken = accessToken;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}