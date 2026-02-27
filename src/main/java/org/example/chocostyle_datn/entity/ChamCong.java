package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "tien_mat_dau_ca")
    private Double tienMatDauCa;

    @Column(name = "tien_chuyen_khoan_dau_ca")
    private Double tienChuyenKhoanDauCa;

    @Column(name = "ghi_chu")
    private String ghiChu;
    @Column(name = "tong_doanh_thu")
    private Double tongDoanhThu;

    @Column(name = "tien_chenh_lech")
    private Double tienChenhLech;
    @Column(name = "doanh_thu_tien_mat")
    private Double doanhThuTienMat;

    @Column(name = "doanh_thu_ck")
    private Double doanhThuCk;

    @Column(name = "chenh_lech_tien_mat")
    private Double chenhLechTienMat;

    @Column(name = "chenh_lech_ck")
    private Double chenhLechCk;
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

    public Double getTienMatDauCa() {
        return tienMatDauCa;
    }

    public void setTienMatDauCa(Double tienMatDauCa) {
        this.tienMatDauCa = tienMatDauCa;
    }

    public Double getTienChuyenKhoanDauCa() {
        return tienChuyenKhoanDauCa;
    }

    public void setTienChuyenKhoanDauCa(Double tienChuyenKhoanDauCa) {
        this.tienChuyenKhoanDauCa = tienChuyenKhoanDauCa;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Double getTienChenhLech() {
        return tienChenhLech;
    }

    public void setTienChenhLech(Double tienChenhLech) {
        this.tienChenhLech = tienChenhLech;
    }

    public Double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(Double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public Double getDoanhThuTienMat() {
        return doanhThuTienMat;
    }

    public void setDoanhThuTienMat(Double doanhThuTienMat) {
        this.doanhThuTienMat = doanhThuTienMat;
    }

    public Double getDoanhThuCk() {
        return doanhThuCk;
    }

    public void setDoanhThuCk(Double doanhThuCk) {
        this.doanhThuCk = doanhThuCk;
    }

    public Double getChenhLechTienMat() {
        return chenhLechTienMat;
    }

    public void setChenhLechTienMat(Double chenhLechTienMat) {
        this.chenhLechTienMat = chenhLechTienMat;
    }

    public Double getChenhLechCk() {
        return chenhLechCk;
    }

    public void setChenhLechCk(Double chenhLechCk) {
        this.chenhLechCk = chenhLechCk;
    }
}