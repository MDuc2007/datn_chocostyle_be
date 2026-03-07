package org.example.chocostyle_datn.model.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ThongKeChiTietResponse {
    private String thoiGian;              // Ví dụ: Hôm nay, Tuần này, Tháng này...
    private BigDecimal doanhThu;          // Doanh thu kỳ hiện tại
    private int soDonHang;                // Số đơn kỳ hiện tại
    private BigDecimal giaTriTrungBinh;   // Giá trị trung bình/đơn
    private Double tangTruong;            // % Tăng trưởng so với kỳ trước
}