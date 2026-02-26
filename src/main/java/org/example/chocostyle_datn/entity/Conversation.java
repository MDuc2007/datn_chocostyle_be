package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang; // Người dùng A (Khách hàng)

    @ManyToOne
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;   // Người dùng B (Nhân viên hỗ trợ)
}