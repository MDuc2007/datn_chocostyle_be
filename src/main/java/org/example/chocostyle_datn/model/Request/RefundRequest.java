package org.example.chocostyle_datn.model.Request;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class RefundRequest {
    private Integer idHoaDon;
    private BigDecimal soTien;
    private String ghiChu;
}