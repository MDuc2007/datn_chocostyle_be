package org.example.chocostyle_datn.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pgg")
    private Integer id;

    @Column(name = "ma_pgg", nullable = false, length = 50)
    private String maPgg;

    @Column(name = "ten_pgg", nullable = false, length = 50)
    private String tenPgg;

    @Column(name = "kieu_ap_dung", nullable = false, length = 20)
    private String kieuApDung;

    @Column(name = "loai_giam", nullable = false, length = 50)
    private String loaiGiam;

    @Column(name = "gia_tri", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "gia_tri_toi_da", precision = 18, scale = 2)
    private BigDecimal giaTriToiDa;

    @Column(name = "dieu_kien_don_hang", precision = 18, scale = 2)
    private BigDecimal dieuKienDonHang;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "so_luong_da_dung", nullable = false)
    private Integer soLuongDaDung;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;
}