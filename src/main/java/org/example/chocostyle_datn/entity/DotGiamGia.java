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

    @NotBlank(message = "Mã đợt giảm giá không được để trống")
    @Size(max = 50)
    @Column(name = "ma_dot_giam_gia", nullable = false, length = 50)
    private String maDotGiamGia;

    @NotBlank(message = "Tên đợt giảm giá không được để trống")
    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_dot_giam_gia", nullable = false)
    private String tenDotGiamGia;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá trị giảm phải ≥ 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Giá trị giảm không được vượt quá 100%")
    @Column(name = "gia_tri_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;
}
