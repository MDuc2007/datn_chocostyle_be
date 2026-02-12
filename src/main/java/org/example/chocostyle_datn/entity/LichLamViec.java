package org.example.chocostyle_datn.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;


@Entity
@Table(name = "lich_lam_viec")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichLamViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lich")
    private Integer id; // Sửa idLich -> id để khớp với Frontend


    // Quan hệ N-1 với NhanVien
    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Tránh lỗi vòng lặp JSON
    private NhanVien nhanVien;


    // Quan hệ N-1 với CaLamViec
    @ManyToOne
    @JoinColumn(name = "id_ca")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CaLamViec caLamViec;


    @Column(name = "ngay_lam_viec")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayLamViec;


    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;


    // 1: Dự kiến (Chưa làm), 2: Đã chấm công, 0: Hủy/Nghỉ
    @Column(name = "trang_thai")
    private Integer trangThai;


    @Column(name = "ma_lap_lai")
    private String maLapLai;
}

