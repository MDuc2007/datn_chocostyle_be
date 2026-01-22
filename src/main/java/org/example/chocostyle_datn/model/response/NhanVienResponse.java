package org.example.chocostyle_datn.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienResponse {
    private Integer id;
    private String maNv;
    private String hoTen;
    private String email;
    private String sdt;
    private String diaChi;
    private String chucVu;
    private Integer trangThai;
    private String avatar;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;

    // --- THÊM CÁC TRƯỜNG NÀY ĐỂ BIND VÀO FORM SỬA ---
    private String diaChiCuThe; // Địa chỉ cụ thể (input text)
    private Integer tinhThanhId;
    private Integer quanHuyenId;
    private Integer xaPhuongId;
}
