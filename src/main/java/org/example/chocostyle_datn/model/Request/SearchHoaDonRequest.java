package org.example.chocostyle_datn.model.Request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class SearchHoaDonRequest {
    private String keyword;    // Mã HĐ, Tên KH, SĐT
    private Integer loaiDon;   // 0: Tại quầy, 1: Online
    private Integer trangThai; // 0: Chờ XN, 1: Đã XN...
    private LocalDate startDate;
    private LocalDate endDate;
}
