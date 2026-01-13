package org.example.chocostyle_datn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @Column(name = "id_pgg", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_pgg", nullable = false, length = 50)
    private String maPgg;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "loai_giam", nullable = false, length = 50)
    private String loaiGiam;

    @NotNull
    @Column(name = "gia_tri", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "gia_tri_toi_da", precision = 18, scale = 2)
    private BigDecimal giaTriToiDa;

    @Column(name = "dieu_kien_don_hang", precision = 18, scale = 2)
    private BigDecimal dieuKienDonHang;

    @NotNull
    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @NotNull
    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;

    @NotNull
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @NotNull
    @Column(name = "so_luong_da_dung", nullable = false)
    private Integer soLuongDaDung;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

}