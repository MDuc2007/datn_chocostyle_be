package org.example.chocostyle_datn.model.Response;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PosSanPhamResponse {
    @Column(name = "id_sp", nullable = false)
    private Integer id;


    @Column(name = "ma_sp", nullable = false, length = 50)
    private String maSp;


    @Column(name = "ten_sp", nullable = false)
    private String tenSp;


    @Column(name = "hinh_anh")
    private String hinhAnh;


    @Column(name = "gia_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaBan;


    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;


    @Column(name = "ten_mau_sac", nullable = false, length = 100)
    private String mauSac;


    @Column(name = "ten_kich_co", nullable = false, length = 100)
    private String kichCo;
}

