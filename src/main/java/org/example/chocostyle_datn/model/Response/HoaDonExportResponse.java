package org.example.chocostyle_datn.model.Response;

import java.math.BigDecimal;

public interface HoaDonExportResponse {
    String getMaHoaDon();
    String getTenKhachHang();
    Integer getLoaiDon();
    Integer getTrangThai();
    String getNgayTao();
    BigDecimal getTongTien();
}