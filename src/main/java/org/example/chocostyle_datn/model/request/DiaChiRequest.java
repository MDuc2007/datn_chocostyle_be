package org.example.chocostyle_datn.model.Request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// File: org.example.chocostyle_datn.model.Request.DiaChiRequest
public class DiaChiRequest {

        private String thanhPho;
        private String quan;
        private String phuong;
        private String diaChiCuThe; // Số nhà, tên đường...
        private Boolean macDinh; // true nếu là địa chỉ chính, false nếu là địa chỉ phụ

}