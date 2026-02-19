package org.example.chocostyle_datn.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;


import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    // 1: Chờ xử lý, 2: Thành công, 3: Thất bại
    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;


    // --- [MỚI] Thêm trường này để hỗ trợ Hoàn Tiền ---
    // 1: Thanh toán (Khách trả tiền), 2: Hoàn tiền (Shop trả lại)
    @Column(name = "loai_giao_dich")
    private Integer loaiGiaoDich;


    @Size(max = 100)
    @Column(name = "ma_yeu_cau", length = 100)
    private String maYeuCau;


    @Size(max = 100)
    @Column(name = "ma_giao_dich_ngoai", length = 100)
    private String maGiaoDichNgoai;


    @Column(name = "thoi_gian_thanh_toan")
    private LocalDateTime thoiGianThanhToan;


    @Size(max = 100)
    @Column(name = "ma_tham_chieu", length = 100)
    private String maThamChieu;


    @Nationalized
    @Column(name = "duong_dan_thanh_toan", columnDefinition = "NVARCHAR(MAX)")
    private String duongDanThanhToan;


    @Nationalized
    @Column(name = "du_lieu_qr", columnDefinition = "NVARCHAR(MAX)")
    private String duLieuQr;


    @Column(name = "thoi_gian_het_han")
    private LocalDateTime thoiGianHetHan;


    @Nationalized
    @Column(name = "du_lieu_phan_hoi", columnDefinition = "NVARCHAR(MAX)")
    private String duLieuPhanHoi;


    @CreationTimestamp // Tự động set ngày tạo
    @Column(name = "thoi_gian_tao", nullable = false, updatable = false)
    private LocalDateTime thoiGianTao;


    @UpdateTimestamp // Tự động set ngày update
    @Column(name = "thoi_gian_cap_nhat")
    private LocalDateTime thoiGianCapNhat;


    @Nationalized
    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;
}

