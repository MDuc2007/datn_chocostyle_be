package org.example.chocostyle_datn.model.Response;


import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonThanhToanResponse {
    private String phuongThuc;
    private BigDecimal soTien;
    private Integer trangThai;
    private String thoiGian;
    private String maGiaoDich;
    private String ghiChu;

    // --- BẠN ĐANG THIẾU DÒNG NÀY ---
    private Integer loaiGiaoDich;
}
