package org.example.chocostyle_datn.repository;

import lombok.Data;
import org.example.chocostyle_datn.model.Response.SanPhamTraCuuDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TraCuuDonHangResponse {
    private Integer id;
    private String maDonHang;
    private LocalDateTime ngayTao;
    private String trangThai; // LƯU Ý: Phải là PENDING, PROCESSING, SHIPPING, DELIVERED hoặc CANCELLED
    private String nguoiNhan;
    private String soDienThoai;
    private String diaChi;
    private BigDecimal tongTienHang;
    private BigDecimal phiVanChuyen;
    private BigDecimal tienGiamGia;
    private BigDecimal tongTienThanhToan;
    private String phuongThucThanhToan;
    private List<SanPhamTraCuuDto> sanPhamList;
}