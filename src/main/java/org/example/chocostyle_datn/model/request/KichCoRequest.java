package org.example.chocostyle_datn.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KichCoRequest {
    private Integer idKichCo;
    private Integer soLuongTon;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
}
