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
import java.time.LocalDateTime; // Fix import

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- Fix lỗi Identifier
    @Column(name = "id_thanh_toan", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pttt", nullable = false)
    private PhuongThucThanhToan idPttt;

    @Size(max = 100)
    @Column(name = "ma_giao_dich", length = 100)
    private String maGiaoDich;

    @NotNull
    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Size(max = 100)
    @Column(name = "ma_yeu_cau", length = 100)
    private String maYeuCau;

    @Size(max = 100)
    @Column(name = "ma_giao_dich_ngoai", length = 100)
    private String maGiaoDichNgoai;

    @Column(name = "thoi_gian_thanh_toan")
    private LocalDateTime thoiGianThanhToan; // Fix: LocalDateTime

    @Size(max = 100)
    @Column(name = "ma_tham_chieu", length = 100)
    private String maThamChieu;

    @Nationalized
    @Lob
    @Column(name = "duong_dan_thanh_toan")
    private String duongDanThanhToan;

    @Nationalized
    @Lob
    @Column(name = "du_lieu_qr")
    private String duLieuQr;

    @Column(name = "thoi_gian_het_han")
    private LocalDateTime thoiGianHetHan; // Fix: LocalDateTime

    @Nationalized
    @Lob
    @Column(name = "du_lieu_phan_hoi")
    private String duLieuPhanHoi;

    @NotNull
    @Column(name = "thoi_gian_tao", nullable = false)
    private LocalDateTime thoiGianTao; // Fix: LocalDateTime

    @Column(name = "thoi_gian_cap_nhat")
    private LocalDateTime thoiGianCapNhat; // Fix: LocalDateTime

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;
}