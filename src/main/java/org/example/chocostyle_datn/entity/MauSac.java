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
@Table(name = "mau_sac")
public class MauSac {
    @Id
    @Column(name = "id_mau_sac", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_mau_sac", nullable = false, length = 50)
    private String maMauSac;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_mau_sac", nullable = false, length = 100)
    private String tenMauSac;

}