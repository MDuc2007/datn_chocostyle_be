package org.example.chocostyle_datn.model.request;

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
    private String hoTen;
    private String email;
    private String sdt;
    private String diaChi;
    // Nhận cả ID và Tên từ Frontend gửi về
    private Integer tinhThanhId;
    private String tinhThanh;

    private Integer quanHuyenId;
    private String quanHuyen;

    private Integer xaPhuongId;
    private String xaPhuong;
    private Boolean gioiTinh;
    private String cccd;
    private String vaiTro;
    private Integer trangThai;
    private LocalDate ngayVaoLam;
    private LocalDate ngaySinh;
    private String avatar;
}
