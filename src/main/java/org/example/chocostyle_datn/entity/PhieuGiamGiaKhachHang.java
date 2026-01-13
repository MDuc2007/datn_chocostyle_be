package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia_khach_hang")
public class PhieuGiamGiaKhachHang {
    @Id
    @Column(name = "id_pggkh", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kh", nullable = false)
    private KhachHang idKh;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pgg", nullable = false)
    private PhieuGiamGia idPgg;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_phieu_giam_gia_kh", nullable = false, length = 50)
    private String maPhieuGiamGiaKh;

    @NotNull
    @Column(name = "ngay_nhan", nullable = false)
    private LocalDate ngayNhan;

    @NotNull
    @Column(name = "da_su_dung", nullable = false)
    private Boolean daSuDung = false;

}