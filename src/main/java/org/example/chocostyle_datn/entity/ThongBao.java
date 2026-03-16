package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "thong_bao")
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_thong_bao")
    private Long id;

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "noi_dung")
    private String noiDung;

    @Column(name = "loai_thong_bao")
    private String loaiThongBao;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "da_doc")
    private Boolean daDoc;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

}