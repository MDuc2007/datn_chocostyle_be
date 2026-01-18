package org.example.chocostyle_datn.model.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangRequest {

        private String avatar; // Trường mới thêm để lưu đường dẫn ảnh
        private String tenKhachHang;
        private String tenTaiKhoan;
        private String soDienThoai;
        private String email;
        private Boolean gioiTinh;
        private LocalDate ngaySinh;
        private String matKhau;
        private Integer trangThai;
        private List<DiaChiRequest> listDiaChi;
    }
