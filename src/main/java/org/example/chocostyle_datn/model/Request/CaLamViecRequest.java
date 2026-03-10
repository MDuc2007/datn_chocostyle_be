package org.example.chocostyle_datn.model.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class CaLamViecRequest {

    @NotBlank(message = "Tên ca không được để trống")
    @Size(min = 2, max = 100, message = "Tên ca làm việc phải từ 2 đến 100 ký tự")
    private String tenCa;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime gioBatDau;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime gioKetThuc;

    private Integer trangThai;
}
