package org.example.chocostyle_datn.model.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BienTheResponse {
    private String maChiTietSanPham;
    private Integer soLuongTon;
    private BigDecimal giaBan;
    private BigDecimal giaNhap;
    private List<String> hinhAnhUrls;
    private List<MauSacResponse> mauSacList;
    private List<String> kichCoList;
    private Integer trangThai;
}
