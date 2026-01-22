package org.example.chocostyle_datn.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateTrangThaiRequest {
    private Integer trangThaiMoi;
    private String ghiChu; // Lý do hủy hoặc ghi chú của admin
    private Integer idNhanVien; // Người thực hiện
}
