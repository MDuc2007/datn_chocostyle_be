package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "dia_chi")
public class DiaChi {
    @Id
    @Column(name = "id_dc", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kh", nullable = false)
    private KhachHang idKh;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_dia_chi", nullable = false, length = 50)
    private String maDiaChi;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_dia_chi", nullable = false)
    private String tenDiaChi;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "thanh_pho", nullable = false, length = 100)
    private String thanhPho;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "quan", nullable = false, length = 100)
    private String quan;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "phuong", nullable = false, length = 100)
    private String phuong;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "dia_chi_cu_the", nullable = false)
    private String diaChiCuThe;

    @NotNull
    @Column(name = "mac_dinh", nullable = false)
    private Boolean macDinh = false;

}