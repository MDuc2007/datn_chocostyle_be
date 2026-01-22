package org.example.chocostyle_datn.model.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DotGiamGiaRequest {

    @NotBlank(message = "Tên đợt giảm giá không được để trống")
    private String tenDotGiamGia;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá trị giảm phải ≥ 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Giá trị giảm không được vượt quá 100%")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate ngayKetThuc;

    @NotEmpty(message = "Phải chọn ít nhất 1 biến thể sản phẩm")
    private List<Integer> chiTietSanPhamIds;
}

