package org.example.chocostyle_datn.model.Response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangResponse {
    private Integer id;
    private String avatar; // Hiển thị ảnh nhỏ (thumbnail) trên bảng nếu cần
    private String maKhachHang;
    private String tenKhachHang;
    private String email;
    private LocalDate ngaySinh;
    private String soDienThoai;
    private String diaChiChinh;
    private Integer trangThai;
    private LocalDate lanMuaGanNhat;

//tesst

}