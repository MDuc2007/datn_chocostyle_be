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
@Table(name = "kich_co")
public class KichCo {
    @Id
    @Column(name = "id_kich_co", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_kich_co", nullable = false, length = 50)
    private String maKichCo;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_kich_co", nullable = false, length = 100)
    private String tenKichCo;

}