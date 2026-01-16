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
@Table(name = "phong_cach_mac")
public class PhongCachMac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phong_cach_mac", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_phong_cach_mac", nullable = false, length = 50)
    private String maPhongCachMac;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_phong_cach", nullable = false)
    private String tenPhongCach;

}