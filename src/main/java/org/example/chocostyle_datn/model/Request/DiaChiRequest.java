package org.example.chocostyle_datn.model.Request;


import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// File: org.example.chocostyle_datn.model.Request.DiaChiRequest
public class DiaChiRequest {
        private Integer khachHangId;
        private String tenDiaChi;
        private String diaChiCuThe;
        private String thanhPho;
        private String quan;
        private String phuong;
        private Boolean macDinh;
}