package org.example.chocostyle_datn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "loai_ao")
public class LoaiAo {
    @Id
    @Column(name = "id_loai_ao", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_loai", nullable = false, length = 50)
    private String maLoai;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_loai", nullable = false)
    private String tenLoai;

}