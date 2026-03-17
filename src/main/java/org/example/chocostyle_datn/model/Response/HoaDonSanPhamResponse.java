package org.example.chocostyle_datn.model.Response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonSanPhamResponse {
    private Integer idSpct;
    private String tenSanPham;
    private String hinhAnh;
    private String mauSac;
    private String kichCo;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    //tesst

}
