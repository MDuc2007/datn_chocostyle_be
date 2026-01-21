package org.example.chocostyle_datn.model.response;

import lombok.*;

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
    private String soDienThoai;
    private String diaChiChinh;
    private Integer trangThai;

}