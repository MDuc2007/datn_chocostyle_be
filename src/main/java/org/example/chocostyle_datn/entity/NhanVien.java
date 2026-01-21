package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nv", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_nv", nullable = false, length = 50)
    private String maNv;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    @Size(max = 20)
    @NotNull
    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Size(max = 20)
    @Column(name = "cccd", length = 20)
    private String cccd;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Nationalized
    @Lob
    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "tinh_thanh_id")
    private Integer tinhThanhId;
    @Column(name = "quan_huyen_id")
    private Integer quanHuyenId;
    @Column(name = "xa_phuong_id")
    private Integer xaPhuongId;

    @Nationalized
    @Column(name = "dia_chi_cu_the")
    private String diaChiCuThe;

    @Nationalized
    @Column(name = "tinh_thanh_ten") private String tinhThanh;

    @Nationalized
    @Column(name = "quan_huyen_ten") private String quanHuyen;

    @Nationalized
    @Column(name = "xa_phuong_ten") private String xaPhuong;
    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "vai_tro", nullable = false, length = 50)
    private String vaiTro;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @Column(name = "ngay_vao_lam", nullable = false)
    private LocalDate ngayVaoLam;

    @NotNull
    @Column(name = "ngay_tao", nullable = false)
    private Date ngayTao;

    @Column(name = "ngay_cap_nhat")
    private Date ngayCapNhat;

    @Column(name = "avatar")
    private String avatar;

    @PrePersist
    public void prePersist() {
        this.ngayTao = new Date(); // Lấy ngày giờ hiện tại
    }

    // Chạy TRƯỚC khi UPDATE vào DB
    @PreUpdate
    public void preUpdate() {
        this.ngayCapNhat = new Date(); // Cập nhật lại ngày sửa đổi
    }
}