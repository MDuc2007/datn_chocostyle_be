package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_spct", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham idSanPham;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kich_co", nullable = false)
    private KichCo idKichCo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mau_sac", nullable = false)
    private MauSac idMauSac;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_loai_ao", nullable = false)
    private LoaiAo idLoaiAo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_phong_cach_mac", nullable = false)
    private PhongCachMac idPhongCachMac;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kieu_dang", nullable = false)
    private KieuDang idKieuDang;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_chi_tiet_san_pham", nullable = false, length = 50)
    private String maChiTietSanPham;

    @NotNull
    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;

    @Column(name = "gia_nhap", precision = 18, scale = 2)
    private BigDecimal giaNhap;

    @NotNull
    @Column(name = "gia_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaBan;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @Column(name = "ngay_tao", nullable = false)
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "nguoi_tao", nullable = false, length = 100)
    private String nguoiTao;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_cap_nhat", length = 100)
    private String nguoiCapNhat;

}