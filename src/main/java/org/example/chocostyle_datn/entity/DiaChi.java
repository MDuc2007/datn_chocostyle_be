package org.example.chocostyle_datn.entity;

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

    @Column(name = "dia_chi_cu_the", nullable = false) // Khớp với lỗi 'column does not allow nulls'
    private String diaChiCuThe;

    // --- CÁC TRƯỜNG LƯU TÊN (HIỂN THỊ) ---
    private String thanhPho;
    private String quan;
    private String phuong;

    // --- QUAN TRỌNG: CÁC TRƯỜNG LƯU ID (DÙNG CHO DROPDOWN KHI SỬA) ---
    // Đảm bảo trong Database của bạn đã có 3 cột này
    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "ward_code")
    private String wardCode;

    @Column(name = "mac_dinh")
    private Boolean macDinh = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kh")
    private KhachHang khachHang;
}