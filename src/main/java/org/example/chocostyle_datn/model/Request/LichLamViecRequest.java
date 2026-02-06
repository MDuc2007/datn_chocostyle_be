package org.example.chocostyle_datn.model.Request;


import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;


@Getter
@Setter
public class LichLamViecRequest {
    private Integer id; // Dùng cho update
    private Integer idNhanVien;
    private Integer idCa;
    private LocalDate ngayLamViec;
    private String ghiChu;
    private Integer trangThai;
}

