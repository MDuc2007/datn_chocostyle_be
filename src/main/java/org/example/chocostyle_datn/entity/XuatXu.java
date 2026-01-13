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
@Table(name = "xuat_xu")
public class XuatXu {
    @Id
    @Column(name = "id_xuat_xu", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_xuat_xu", nullable = false, length = 50)
    private String maXuatXu;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_xuat_xu", nullable = false)
    private String tenXuatXu;

}