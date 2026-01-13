package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "san_pham")
public class SanPham {
    @Id
    @Column(name = "id_sp", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_xuat_xu", nullable = false)
    private XuatXu idXuatXu;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_chat_lieu", nullable = false)
    private ChatLieu idChatLieu;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_sp", nullable = false, length = 50)
    private String maSp;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_sp", nullable = false)
    private String tenSp;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Nationalized
    @Lob
    @Column(name = "hinh_anh")
    private String hinhAnh;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @Column(name = "ngay_tao", nullable = false)
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "nguoi_tao", nullable = false, length = 100)
    private String nguoiTao;

    @Size(max = 100)
    @Nationalized
    @Column(name = "nguoi_cap_nhat", length = 100)
    private String nguoiCapNhat;

}