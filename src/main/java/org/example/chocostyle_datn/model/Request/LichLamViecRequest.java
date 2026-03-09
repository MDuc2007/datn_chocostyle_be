package org.example.chocostyle_datn.model.Request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class LichLamViecRequest {
    private Integer id; // Dùng cho update

    @NotNull(message = "Vui lòng chọn nhân viên")
    private Integer idNhanVien;

    @NotNull(message = "Vui lòng chọn ca làm việc")
    private Integer idCa;

    @NotNull(message = "Ngày làm việc không được để trống")
    @FutureOrPresent(message = "Không được xếp lịch cho ngày trong quá khứ")
    private LocalDate ngayLamViec;

    private String ghiChu;
    private Integer trangThai;
}

