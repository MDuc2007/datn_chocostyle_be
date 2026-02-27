package org.example.chocostyle_datn.model.Response;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamTraCuuDto {
    private String hinhAnh;
    private String tenSp;
    private String mauSac;
    private String kichCo;
    private Integer soLuong;
    private BigDecimal giaBan;
}