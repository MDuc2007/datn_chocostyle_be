package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cau_hinh_he_thong")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CauHinhHeThong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email_nhan")
    private String emailNhan;

    @Column(name = "gui_ngay")
    private Boolean guiNgay;

    @Column(name = "gui_tuan")
    private Boolean guiTuan;

    @Column(name = "gui_thang")
    private Boolean guiThang;

    @Column(name = "gui_nam")
    private Boolean guiNam;
}