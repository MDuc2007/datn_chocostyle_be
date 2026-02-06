package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dot_giam_gia")
    private Integer id;
    @Size(max = 50)
    @Column(name = "ma_dot_giam_gia", nullable = false, length = 50)
    private String maDotGiamGia;
    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_dot_giam_gia", nullable = false)
    private String tenDotGiamGia;
    @Column(name = "gia_tri_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriGiam;
    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;
    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;
}
