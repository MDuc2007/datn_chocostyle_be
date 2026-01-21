package org.example.chocostyle_datn.model.Response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KhachHangDetailResponse {

    // --- THÔNG TIN KHÁCH HÀNG (Bảng khach_hang) ---
    private Integer id;             // Ánh xạ id_kh
    private String avatar;          // Đã thêm trường avatar theo yêu cầu
    private String maKhachHang;     // ma_kh
    private String tenKhachHang;    // ten_khach_hang (Họ và tên trên Form)
    private String tenTaiKhoan;     // ten_tai_khoan (Username)
    private String soDienThoai;     // so_dien_thoai
    private String email;           // email

    // Giữ lại để dự phòng hoặc hiển thị chuỗi địa chỉ nối liền
    private String diaChiTongQuat;

    private Boolean gioiTinh;       // true: Nam, false: Nữ
    private LocalDate ngaySinh;     // ngay_sinh
    private String matKhau;         // mat_khau (Dùng để hiển thị hoặc điền vào ô mật khẩu khi sửa)

    private Integer trangThai;      // trang_thai
    private LocalDate ngayTao;
    private LocalDate ngayCapNhat;

    private Integer soLuongDonHang;
    private BigDecimal tongChiTieu;

    // --- DANH SÁCH ĐỊA CHỈ (Bảng dia_chi) ---
    // FE sẽ dùng list này để render ra các block địa chỉ có nút "Mặc định"
    private List<DiaChiDetailResponse> listDiaChi;

    // --- Class con (Inner Class) để map dữ liệu bảng dia_chi ---
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiaChiDetailResponse {
        private Integer id;             // id_dc
        // Đã loại bỏ tenDiaChi theo yêu cầu của bạn
        private String thanhPho;        // thanh_pho (Tỉnh/TP)
        private String quan;            // quan (Quận/Huyện)
        private String phuong;          // phuong (Phường/Xã)
        private String diaChiCuThe;     // dia_chi_cu_the (Số nhà, đường...)
        private Boolean macDinh;        // mac_dinh (Xác định địa chỉ chính)
    }
}