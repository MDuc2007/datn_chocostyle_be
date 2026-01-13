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
@Table(name = "dot_giam_gia")
public class DotGiamGia {
    @Id
    @Column(name = "id_dot_giam_gia", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_dot_giam_gia", nullable = false, length = 50)
    private String maDotGiamGia;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_dot_giam_gia", nullable = false)
    private String tenDotGiamGia;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "loai_giam_gia", nullable = false, length = 50)
    private String loaiGiamGia;

    @NotNull
    @Column(name = "gia_tri_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    @NotNull
    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @NotNull
    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "so_tien_toi_da", precision = 18, scale = 2)
    private BigDecimal soTienToiDa;

}