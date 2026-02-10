package org.example.chocostyle_datn.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "v_khach_hang_thong_ke")
@Getter
@Setter
public class KhachHangThongKe {


    @Id
    @Column(name = "id_kh")
    private Integer idKh;


    @Column(name = "ma_kh")
    private String maKh;


    @Column(name = "ten_khach_hang")
    private String tenKhachHang;


    private String email;


    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;


    @Column(name = "trang_thai")
    private Integer trangThai;


    @Column(name = "tong_don_hang")
    private Integer tongDonHang;


    @Column(name = "tong_chi_tieu")
    private BigDecimal tongChiTieu;


    @Column(name = "lan_mua_gan_nhat")
    private LocalDateTime lanMuaGanNhat;
}



