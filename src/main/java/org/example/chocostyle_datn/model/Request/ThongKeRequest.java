package org.example.chocostyle_datn.model.Request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


import java.time.LocalDate;


@Data
public class ThongKeRequest {
    // Dùng LocalDate để Spring tự convert chuỗi 'yyyy-MM-dd' từ Frontend
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;


    // Có thể thêm loại thống kê nếu cần (theo ngày, tháng, năm)
    private String type;
}

