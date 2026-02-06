package org.example.chocostyle_datn.repository;

import java.math.BigDecimal;

public interface SanPhamBanChayProjection {
    Integer getId();
    String getTenSp();
    String getHinhAnh();
    BigDecimal getGiaMin();
    BigDecimal getGiaMax();
    Long getSoLuongDaBan();
}

