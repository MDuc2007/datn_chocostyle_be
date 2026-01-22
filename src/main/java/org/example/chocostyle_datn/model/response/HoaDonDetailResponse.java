package org.example.chocostyle_datn.model.response;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonDetailResponse {
    // 1. Thông tin chung
    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private String tenNhanVien;
    private Integer trangThai;
    private Integer loaiDon;
    private LocalDate ngayTao;
    private String ghiChu;

    // 2. Thông tin tiền
    private BigDecimal tongTienHang;
    private BigDecimal phiShip;
    private BigDecimal giamGia;
    private BigDecimal tongThanhToan;

    // 3. Các danh sách chi tiết (Sử dụng các class vừa tách)
    private List<HoaDonSanPhamResponse> sanPhamList;
    private List<HoaDonLichSuResponse> lichSuList;
    private List<HoaDonThanhToanResponse> thanhToanList;
}
