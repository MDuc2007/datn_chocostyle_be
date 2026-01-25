package org.example.chocostyle_datn.model.Response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiaChiResponse {

    private Integer id;             // Map với id_dc (Dùng để xác định ID khi sửa/xóa)
    private String maDiaChi;        // Map với ma_dia_chi
    private String tenDiaChi;       // Ví dụ: "Nhà riêng", "Văn phòng"

    // --- CÁC TRƯỜNG HIỂN THỊ (TEXT) ---
    private String thanhPho;        // Tên Tỉnh/Thành phố
    private String quanHuyen;       // Tên Quận/Huyện
    private String phuongXa;        // Tên Phường/Xã
    private String diaChiCuThe;     // Số nhà, tên đường...

    // --- CÁC TRƯỜNG ĐỊNH DANH (ID - Quan trọng cho Dropdown Frontend) ---
    private Integer provinceId;     // ID Tỉnh/Thành phố
    private Integer districtId;     // ID Quận/Huyện
    private String wardCode;        // Mã Phường/Xã

    private Boolean macDinh;        // Trạng thái địa chỉ mặc định
    //tesst

}