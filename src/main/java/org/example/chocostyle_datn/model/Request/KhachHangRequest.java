package org.example.chocostyle_datn.model.Request;


import lombok.*;
import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangRequest {


        private String avatar;


        private String tenKhachHang;


        private String soDienThoai;


        private String email; // Quan trọng: Dùng để login & nhận mật khẩu


        private Boolean gioiTinh;


        private LocalDate ngaySinh;


        private Integer trangThai;


        // Danh sách địa chỉ gửi từ FE (Tỉnh, Huyện, Xã...)
        private List<DiaChiRequest> listDiaChi;


        // Inner class cho Địa chỉ (Để mapping JSON dễ dàng)
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DiaChiRequest {
                private Integer id; // Dùng khi update
                private String thanhPho;
                private String quan;
                private String phuong;
                private String diaChiCuThe;
                private Boolean macDinh;
        }
}

