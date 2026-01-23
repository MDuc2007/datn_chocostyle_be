package org.example.chocostyle_datn.model.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class HoaDonResponse {
    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String tenNhanVien;
    private BigDecimal tongTien;
    private Integer loaiDon;
    private Integer trangThai;
    private LocalDate ngayTao;
}
