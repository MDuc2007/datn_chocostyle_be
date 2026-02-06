package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // Quan trọng: Dùng LocalDateTime

@Getter
@Setter
@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- Fix lỗi Identifier
    @Column(name = "id_hoa_don")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    // Nếu có quan hệ với phiếu giảm giá thì bỏ comment dòng dưới
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "id_phieu_giam_gia")
     private PhieuGiamGia idPhieuGiamGia;

    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "loai_don")
    private Integer loaiDon;

    @Column(name = "tong_tien_goc")
    private BigDecimal tongTienGoc;

    @Column(name = "tong_tien_thanh_toan")
    private BigDecimal tongTienThanhToan;

    // --- Bổ sung các trường thiếu để Service không báo lỗi ---
    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;

    @Column(name = "so_tien_giam")
    private BigDecimal soTienGiam;
    // -------------------------------------------------------

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao; // Fix: LocalDateTime

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan; // Fix: LocalDateTime

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat; // Fix: LocalDateTime

    @Column(name = "ten_khach_hang")
    private String tenKhachHang;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "dia_chi_khach_hang")
    private String diaChiKhachHang;

    @Column(name = "ghi_chu")
    private String ghiChu;
}