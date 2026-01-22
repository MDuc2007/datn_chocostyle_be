package org.example.chocostyle_datn.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaResponse {
    private Integer id;
    private String maPgg;
    private String tenPgg;
    private String kieuApDung;
    private String loaiGiam;
    private BigDecimal giaTri;
    private BigDecimal giaTriToiDa;
    private BigDecimal dieuKienDonHang;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer soLuong;
    private Integer soLuongDaDung;
    private Integer trangThai;
}

