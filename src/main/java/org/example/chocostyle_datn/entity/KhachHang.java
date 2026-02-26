package org.example.chocostyle_datn.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kh", nullable = false)
    private Integer id;


    @Size(max = 50)
    @Column(name = "ma_kh", unique = true, nullable = false)
    private String maKh; // Backend tự sinh


    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_khach_hang", nullable = false)
    private String tenKhachHang;




    @Size(max = 20)
    @Column(name = "so_dien_thoai", length = 20, nullable = false)
    private String soDienThoai;


    @Size(max = 100)
    @Column(name = "email", length = 100, nullable = false)
    private String email; // Bắt buộc có để nhận mật khẩu


    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;


    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;


    @Size(max = 255)
    @Column(name = "mat_khau")
    private String matKhau; // Backend tự random


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
    private Integer soLuongDonHang = 0;


    @Column(name = "tong_chi_tieu")
    private BigDecimal tongChiTieu = BigDecimal.ZERO;

    @Column(name = "dia_chi")
    private String diaChi;

    // --- OAUTH2 ---
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthenticationProvider authProvider;


    @Column(name = "provider_id")
    private String providerId;


    // Quan hệ 1-N với bảng Địa chỉ
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiaChi> listDiaChiObj = new ArrayList<>();


    @Column(name = "vai_tro")
    private String vaiTro;


    @PrePersist
    public void prePersist() {
        this.ngayTao = LocalDate.now();
        if (this.trangThai == null) this.trangThai = 1;
        if (this.authProvider == null) this.authProvider = AuthenticationProvider.LOCAL;
        if (this.vaiTro == null) this.vaiTro = "USER"; // Mặc định role USER


        if (this.soLuongDonHang == null) this.soLuongDonHang = 0;
        if (this.tongChiTieu == null) this.tongChiTieu = BigDecimal.ZERO;
    }


    @PreUpdate
    public void preUpdate() {
        this.ngayCapNhat = LocalDate.now();
    }
}

