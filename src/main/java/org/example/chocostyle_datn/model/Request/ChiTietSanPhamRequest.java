package org.example.chocostyle_datn.model.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChiTietSanPhamRequest {
    private Integer idSanPham;
    private Integer idKichCo;
    private Integer idMauSac;
    private Integer idLoaiAo;
    private Integer idPhongCachMac;
    private Integer idKieuDang;

    private Integer soLuongTon;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer trangThai;

    private String nguoiTao;
    private String nguoiCapNhat;

    // 👉 URL ảnh Cloudinary
    private List<String> hinhAnh;
    //tesst

}
