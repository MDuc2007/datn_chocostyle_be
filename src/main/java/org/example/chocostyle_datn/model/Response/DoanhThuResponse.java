package org.example.chocostyle_datn.model.Response;


import java.math.BigDecimal;


public interface DoanhThuResponse {
    String getThoiGian();     // Trả về ngày (yyyy-MM-dd)
    BigDecimal getDoanhThu(); // Tổng tiền thanh toán
    Integer getSoLuongDon();  // Số lượng đơn hàng
}

