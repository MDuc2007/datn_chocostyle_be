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
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- THÊM DÒNG NÀY
    @Column(name = "id_kh", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_kh", unique = true) // unique để không trùng
    private String maKh;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_khach_hang", nullable = false)
    private String tenKhachHang;

    @Size(max = 100)
    @Column(name = "ten_tai_khoan", length = 100)
    private String tenTaiKhoan;

    @Size(max = 20)
    @NotNull
    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Nationalized
    @Lob
    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Size(max = 255)
    @Column(name = "mat_khau")
    private String matKhau;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @Column(name = "ngay_tao", nullable = false)
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "so_luong_don_hang")
    private Integer soLuongDonHang;

    @Column(name = "tong_chi_tieu")
    private BigDecimal tongChiTieu;
    // Thêm quan hệ OneToMany
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiaChi> listDiaChiObj = new ArrayList<>();

}