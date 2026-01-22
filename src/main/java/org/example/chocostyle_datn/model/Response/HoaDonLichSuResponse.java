package org.example.chocostyle_datn.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonLichSuResponse {
    private Integer trangThai;
    private String hanhDong;
    private String ghiChu;
    private String thoiGian;
    private String nguoiThucHien;
}
