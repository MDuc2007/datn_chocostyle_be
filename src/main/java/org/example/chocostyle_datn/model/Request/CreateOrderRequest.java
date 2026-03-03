package org.example.chocostyle_datn.model.Request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    private Integer idKhachHang;
    //    private Integer idNhanVien;
    private Integer loaiDon; // 0: Online, 1: Tại quầy
    private String ghiChu;
    private BigDecimal tongTienHang;   // Tổng tiền chưa giảm
    private BigDecimal phiShip;
    private String maVoucher;
    private List<CartItemRequest> sanPhamChiTiet;
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String emailNguoiNhan; // Quan trọng để gửi mail
    private String diaChiGiaoHang;
    private Integer phuongThuc;

}