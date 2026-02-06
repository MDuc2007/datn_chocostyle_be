package org.example.chocostyle_datn.model.Response;

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
public class ChiTietSanPhamResponse {
    private Integer id;
    private String maChiTietSanPham;

    private Integer soLuongTon;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer trangThai;

    private String tenMauSac;
    private String tenKichCo;
    private String tenLoaiAo;
    private String tenPhongCachMac;
    private String tenKieuDang;
    private String tenSanPham;
    private String maSanPham;

    private List<String> hinhAnh;
    private LocalDate ngayTao;
    private String nguoiTao;
    private LocalDate ngayCapNhat;
    private String nguoiCapNhat;
    private String qrCode;
    private String qrImage;
    //tesst

}
