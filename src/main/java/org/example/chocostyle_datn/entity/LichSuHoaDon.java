package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime; // Sửa import thành LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- QUAN TRỌNG: Thêm dòng này để ID tự tăng
    @Column(name = "id_lich_su_hoa_don", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon; // Tên biến là idHoaDon -> Setter sẽ là setIdHoaDon

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian; // <--- Sửa thành LocalDateTime để lưu cả giờ phút

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "hanh_dong", nullable = false)
    private String hanhDong;
}