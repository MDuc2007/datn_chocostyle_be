package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lich_su_thanh_toan")
public class LichSuThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lich_su_thanh_toan", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon;

    @Size(max = 100)
    @Column(name = "ma_giao_dich", length = 100)
    private String maGiaoDich;

    @NotNull
    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @NotNull
    @Column(name = "ngay_thanh_toan", nullable = false)
    private LocalDate ngayThanhToan;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "hinh_thuc_thanh_toan", nullable = false, length = 50)
    private String hinhThucThanhToan;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "loai_thanh_toan", nullable = false, length = 100)
    private String loaiThanhToan;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "trang_thai", nullable = false, length = 50)
    private String trangThai;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ghi_chu")
    private String ghiChu;

}