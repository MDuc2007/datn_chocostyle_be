package org.example.chocostyle_datn.model.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaRequest {

    @NotNull(message = "Tên phiếu giảm giá không được để trống")
    @Size(max = 50, message = "Tên phiếu giảm giá tối đa 50 ký tự")
    private String tenPgg;

    @NotNull(message = "Kiểu áp dụng không được để trống")
    private String kieuApDung;

    @NotNull(message = "Loại giảm không được để trống")
    private String loaiGiam; // PERCENT | MONEY

    @NotNull(message = "Giá trị giảm không được để trống")
    private BigDecimal giaTri;

    private BigDecimal giaTriToiDa;

    private BigDecimal dieuKienDonHang;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate ngayKetThuc;

    @NotNull(message = "Số lượng không được để trống")
    private Integer soLuong;

}

