package org.example.chocostyle_datn.entity;


import com.fasterxml.jackson.annotation.JsonIgnore; // 👉 IMPORT THÊM DÒNG NÀY
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Entity
@Table(name = "dia_chi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dc")
    private Integer id;


    @Column(name = "ma_dia_chi")
    private String maDiaChi;


    @Column(name = "ten_dia_chi")
    private String tenDiaChi;


    @Column(name = "dia_chi_cu_the", nullable = false)
    private String diaChiCuThe;


    @Column(name = "thanh_pho", nullable = false)
    private String thanhPho;


    @Column(name = "quan", nullable = false)
    private String quan;


    @Column(name = "phuong", nullable = false)
    private String phuong;


    @Column(name = "mac_dinh")
    private Boolean macDinh = false;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kh")
    @JsonIgnore // 👉 THÊM ANNOTATION NÀY ĐỂ CẮT VÒNG LẶP
    private KhachHang khachHang;
}

