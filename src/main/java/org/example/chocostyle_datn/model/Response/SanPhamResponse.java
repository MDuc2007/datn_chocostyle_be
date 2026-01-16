package org.example.chocostyle_datn.model.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SanPhamResponse {
    private Integer id;
    private String maSp;
    private String tenSp;
    private String moTa;
    private Integer trangThai;

    private String tenChatLieu;
    private String tenXuatXu;
    private String tenLoaiAo;
    private String tenKieuDang;
    private String tenPhongCachMac;
    private List<BienTheResponse> bienTheList;
    private List<String> hinhAnhUrls;

    private LocalDate ngayTao;
}
