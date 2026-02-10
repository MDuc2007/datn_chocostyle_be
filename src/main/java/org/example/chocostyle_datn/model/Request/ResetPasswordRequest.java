package org.example.chocostyle_datn.model.Request;


import lombok.Data;
import org.example.chocostyle_datn.entity.ResetAccountType;


import jakarta.validation.constraints.*;


@Data
public class ResetPasswordRequest {


    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;


    @NotBlank(message = "OTP không được để trống")
    @Pattern(regexp = "\\d{6}", message = "OTP phải gồm 6 chữ số")
    private String otp;


    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu phải tối thiểu 6 ký tự")
    private String newPassword;


    @NotNull(message = "Loại tài khoản không được để trống")
    private ResetAccountType type;
}

