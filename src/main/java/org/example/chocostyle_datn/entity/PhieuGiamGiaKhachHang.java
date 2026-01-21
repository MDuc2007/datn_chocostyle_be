package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia_khach_hang")
public class PhieuGiamGiaKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pggkh")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_kh", nullable = false)
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_pgg", nullable = false)
    private PhieuGiamGia phieuGiamGia;

    @Column(name = "ma_phieu_giam_gia_kh", nullable = false)
    private String maPhieuGiamGiaKh;

    @Column(name = "ngay_nhan", nullable = false)
    private LocalDateTime ngayNhan;

    @Column(name = "da_su_dung", nullable = false)
    private Boolean daSuDung;
}
