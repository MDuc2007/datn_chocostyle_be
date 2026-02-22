package org.example.chocostyle_datn.model.Response;

import lombok.Data;

@Data
public class GiaoCaResponse {
    private Integer id;
    private String nhanVien;
    private String ca;
    private String thoiGianMo;
    private String thoiGianDong;
    private Double tienMat;
    private Double tienChuyenKhoan;
    private Double tongDoanhThu;
    private Double tienChenh;
    private Integer trangThai;
}
