package org.example.chocostyle_datn.model.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamHomeListResponse {

    private Integer id;
    private String tenSp;
    private String hinhAnh;

    // Giá hiển thị
    private BigDecimal giaMin;
    private BigDecimal giaMax;

    // Chỉ dùng cho bán chạy (list thường = null)
    private Long soLuongDaBan;
}

