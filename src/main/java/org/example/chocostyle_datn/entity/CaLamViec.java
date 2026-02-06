package org.example.chocostyle_datn.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(name = "ca_lam_viec")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaLamViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ca")
    private Integer idCa;


    @Column(name = "ma_ca", nullable = false, length = 20, unique = true)
    private String maCa;


    @Column(name = "ten_ca", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenCa;


    @Column(name = "gio_bat_dau", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss") // Format để FE gửi/nhận dữ liệu chuẩn
    private LocalTime gioBatDau;


    @Column(name = "gio_ket_thuc", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime gioKetThuc;


    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;


    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;


    @Column(name = "trang_thai")
    private Integer trangThai; // 1: Hoạt động, 0: Ngưng
}

