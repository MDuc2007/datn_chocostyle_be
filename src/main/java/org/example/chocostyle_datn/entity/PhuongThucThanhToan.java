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
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pttt", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ma_pttt", nullable = false, length = 50)
    private String maPttt;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "ten_pttt", nullable = false)
    private String tenPttt;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

}