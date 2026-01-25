package org.example.chocostyle_datn.model.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DotGiamGiaResponse {
    private Integer id;
    private String maDotGiamGia;
    private String tenDotGiamGia;
    private BigDecimal giaTriGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer trangThai;
    private List<Integer> chiTietSanPhamIds;
    private List<DotGiamGiaSanPhamResponse> sanPhamApDung;
}