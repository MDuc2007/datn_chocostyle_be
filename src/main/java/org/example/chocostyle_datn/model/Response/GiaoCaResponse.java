package org.example.chocostyle_datn.model.Response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    private Double tienMatDauCa;
    private Double tienChuyenKhoanDauCa;
    private Double tienChenhLech;
    private String ghiChu;
}
