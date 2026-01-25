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
import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chi_tiet_dot_giam_gia")
public class ChiTietDotGiamGia {
    @EmbeddedId
    private ChiTietDotGiamGiaId id;

    @MapsId("idDotGiamGia")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_dot_giam_gia", nullable = false)
    private DotGiamGia idDotGiamGia;

    @MapsId("idSpct")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_spct", nullable = false)
    private ChiTietSanPham idSpct;

    @NotNull
    @Column(name = "so_luong_ton_kho_khuyen_mai", nullable = false)
    private Integer soLuongTonKhoKhuyenMai;

    @Column(name = "gia_tri_giam_toi_thieu", precision = 18, scale = 2)
    private BigDecimal giaTriGiamToiThieu;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_cap_nhat", length = 100)
    private String nguoiCapNhat;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @Column(name = "ngay_tao", nullable = false)
    private LocalDate ngayTao;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "nguoi_tao", nullable = false, length = 100)
    private String nguoiTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

}