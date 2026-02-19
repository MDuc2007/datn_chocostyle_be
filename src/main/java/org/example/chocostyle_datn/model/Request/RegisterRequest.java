package org.example.chocostyle_datn.model.Request;




import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;




@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;




    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$",
            message = "Số điện thoại không hợp lệ (phải 10 số và bắt đầu bằng 0)")
    private String soDienThoai;




    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;




    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String matKhau;




}



