package org.example.chocostyle_datn.model.Request;




import jakarta.validation.constraints.NotBlank;
import lombok.Data;




@Data
public class LoginRequest {
    @NotBlank(message = "Tên tài khoản hoặc Email không được để trống")
    private String email; // Frontend có thể gửi user hoặc email vào đây




    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}







