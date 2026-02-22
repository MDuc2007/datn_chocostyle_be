package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "cham_cong")
public class ChamCong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien", nullable = false)
    private NhanVien nhanVien;

    @Column(nullable = false)
    private LocalDate ngay;

    @Column(name = "gio_check_in")
    private LocalTime gioCheckIn;

    @Column(name = "gio_check_out")
    private LocalTime gioCheckOut;

    /*
        1 = Đã check-in
        2 = Đi trễ
        3 = Đã check-out
    */
    @Column(name = "trang_thai")
    private Integer trangThai;

    // 👉 THÊM 2 CỘT NÀY ĐỂ LƯU TIỀN KẾT TOÁN CUỐI CA
    @Column(name = "tien_mat_cuoi_ca")
    private Double tienMatCuoiCa;

    @Column(name = "tien_chuyen_khoan_cuoi_ca")
    private Double tienChuyenKhoanCuoiCa;


    // ===== Getter & Setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        this.ngay = ngay;
    }

    public LocalTime getGioCheckIn() {
        return gioCheckIn;
    }

    public void setGioCheckIn(LocalTime gioCheckIn) {
        this.gioCheckIn = gioCheckIn;
    }

    public LocalTime getGioCheckOut() {
        return gioCheckOut;
    }

    public void setGioCheckOut(LocalTime gioCheckOut) {
        this.gioCheckOut = gioCheckOut;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    // 👉 THÊM GETTER & SETTER CHO 2 BIẾN TIỀN
    public Double getTienMatCuoiCa() {
        return tienMatCuoiCa;
    }

    public void setTienMatCuoiCa(Double tienMatCuoiCa) {
        this.tienMatCuoiCa = tienMatCuoiCa;
    }

    public Double getTienChuyenKhoanCuoiCa() {
        return tienChuyenKhoanCuoiCa;
    }

    public void setTienChuyenKhoanCuoiCa(Double tienChuyenKhoanCuoiCa) {
        this.tienChuyenKhoanCuoiCa = tienChuyenKhoanCuoiCa;
    }
}