package org.example.chocostyle_datn.model.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 5, max = 100, message = "Họ tên phải từ 5 đến 100 ký tự")
    @Pattern(regexp = "^[^0-9!@#$%^&*()_+={}\\[\\]:;\"'<>,.?/\\\\|`~\\-]+$", message = "Họ tên không được chứa số hoặc ký tự đặc biệt")
    private String hoTen;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(84|0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại không hợp lệ (Phải là 10 số, đầu VN)")
    private String sdt;

    private String diaChi; // Trường này tự ghép ở Service, không cần validate từ Client

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Size(max = 255, message = "Địa chỉ quá dài")
    private String diaChiCuThe;

    @NotNull(message = "Chưa chọn Tỉnh/Thành phố")
    private Integer tinhThanhId;
    private String tinhThanh;

    @NotNull(message = "Chưa chọn Quận/Huyện")
    private Integer quanHuyenId;
    private String quanHuyen;

    @NotNull(message = "Chưa chọn Xã/Phường")
    private Integer xaPhuongId;
    private String xaPhuong;

    @NotNull(message = "Giới tính không được để trống")
    private Boolean gioiTinh;

    private String vaiTro;

    private Integer trangThai;

    private LocalDate ngayVaoLam;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate ngaySinh;

    private String avatar;
}