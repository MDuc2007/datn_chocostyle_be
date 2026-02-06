package org.example.chocostyle_datn.model.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate; // hoặc LocalDateTime tùy kiểu dữ liệu
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonResponse {
    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;

    // --- BỔ SUNG FIELD NÀY ---
    private String soDienThoai;
    // -------------------------

    private String tenNhanVien; // Có thể giữ lại hoặc xóa tùy nhu cầu
    private BigDecimal tongTien;
    private Integer loaiDon;
    private Integer trangThai;
    private LocalDateTime ngayTao; // Lưu ý: Entity dùng DATETIME[cite: 201], nên check xem DTO đang để LocalDate hay LocalDateTime
}