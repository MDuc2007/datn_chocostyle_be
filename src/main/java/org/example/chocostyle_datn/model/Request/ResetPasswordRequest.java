package org.example.chocostyle_datn.model.Request;


import lombok.Data;


@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
    private String email;
}

