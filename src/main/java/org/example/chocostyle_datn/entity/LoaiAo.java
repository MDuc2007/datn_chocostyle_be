package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loai_ao")
public class LoaiAo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loai_ao", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_loai", nullable = false, length = 50)
    private String maLoai;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_loai", nullable = false)
    private String tenLoai;

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