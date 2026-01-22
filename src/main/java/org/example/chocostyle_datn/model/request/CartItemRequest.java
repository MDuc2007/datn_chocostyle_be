package org.example.chocostyle_datn.model.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CartItemRequest {
    private Integer idChiTietSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
}