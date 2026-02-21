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
    private String thoiGian;
    private Integer trangThai;
    //tesst

}
