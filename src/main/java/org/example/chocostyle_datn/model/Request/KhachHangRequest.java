package org.example.chocostyle_datn.model.Request;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangRequest {
        private String avatar; // Trường mới thêm để lưu đường dẫn ảnh
        private String tenKhachHang;
        private String tenTaiKhoan;
        private String soDienThoai;
        private String email;
        private Boolean gioiTinh;
        private LocalDate ngaySinh;
        private String matKhau;
        private Integer trangThai;
        private List<DiaChiRequest> listDiaChi;
    }
