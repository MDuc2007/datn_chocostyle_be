package org.example.chocostyle_datn.model.Response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TongQuatResponse {
    private BigDecimal doanhThu;
    private Integer soDonHang;
    private Long soSanPhamDaBan;
    // Có thể thêm % tăng trưởng nếu muốn
    private Double tangTruong;
}

