package org.example.chocostyle_datn.model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonThanhToanResponse {
    private String phuongThuc;
    private BigDecimal soTien;
    private Integer trangThai;
    private String thoiGian;

    // THÊM 3 TRƯỜNG MỚI NÀY ĐỂ HẾT LỖI
    private Integer loaiGiaoDich;
    private String maGiaoDich;
    private String ghiChu;
}