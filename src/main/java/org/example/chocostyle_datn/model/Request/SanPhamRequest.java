package org.example.chocostyle_datn.model.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SanPhamRequest {
    private String maSp;
    private String tenSp;
    private String moTa;
    private Integer trangThai;
    private Integer idChatLieu;
    private Integer idXuatXu;
    private String hinhAnh;

    private Integer idLoaiAo;
    private Integer idKieuDang;
    private Integer idPhongCachMac;
    private List<BienTheRequest> bienTheList;
    private String nguoiTao;
    private String nguoiCapNhat;
    //tesst


}
