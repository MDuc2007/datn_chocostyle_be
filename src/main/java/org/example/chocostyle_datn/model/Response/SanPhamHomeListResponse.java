package org.example.chocostyle_datn.model.Response;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
public class SanPhamHomeListResponse {
    private Integer id;
    private String tenSp;
    private String hinhAnh;
    private BigDecimal giaMin;
    private BigDecimal giaMax;
    private Long soLuongDaBan;
    private Integer phanTramGiam;
    private BigDecimal giaSauGiam;

    // CONSTRUCTOR DÙNG CHO CÂU LỆNH NATIVE SQL (Best Seller)
    public SanPhamHomeListResponse(Integer id, String tenSp, String hinhAnh, BigDecimal giaMin, BigDecimal giaMax, Long soLuongDaBan, Integer phanTramGiam) {
        this.id = id;
        this.tenSp = tenSp;
        this.hinhAnh = hinhAnh;
        this.giaMin = giaMin;
        this.giaMax = giaMax;
        this.soLuongDaBan = soLuongDaBan;
        setPhanTramGiamAndTinhGia(phanTramGiam, giaMin);
    }

    // CONSTRUCTOR MỚI DÙNG CHO JPQL (Tất Cả Sản Phẩm)
    // Dùng kiểu Number để Hibernate tự do truyền kiểu số vào mà không báo lỗi Missing Constructor
    public SanPhamHomeListResponse(Integer id, String tenSp, String hinhAnh, BigDecimal giaMin, BigDecimal giaMax, Number soLuongDaBan, Number phanTramGiam) {
        this.id = id;
        this.tenSp = tenSp;
        this.hinhAnh = hinhAnh;
        this.giaMin = giaMin;
        this.giaMax = giaMax;

        this.soLuongDaBan = soLuongDaBan != null ? soLuongDaBan.longValue() : null;
        Integer phanTram = phanTramGiam != null ? phanTramGiam.intValue() : null;

        setPhanTramGiamAndTinhGia(phanTram, giaMin);
    }

    // Hàm tiện ích để tính toán giá
    private void setPhanTramGiamAndTinhGia(Integer phanTram, BigDecimal giaMinBanDau) {
        this.phanTramGiam = phanTram;
        if (phanTram != null && phanTram > 0 && giaMinBanDau != null) {
            BigDecimal mucGiam = giaMinBanDau.multiply(new BigDecimal(phanTram)).divide(new BigDecimal(100));
            this.giaSauGiam = giaMinBanDau.subtract(mucGiam).setScale(0, RoundingMode.HALF_UP);
        } else {
            this.giaSauGiam = giaMinBanDau;
        }
    }
}