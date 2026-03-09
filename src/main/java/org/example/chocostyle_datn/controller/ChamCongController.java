package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.entity.ChamCong;
import org.example.chocostyle_datn.repository.ChamCongRepository;
import org.example.chocostyle_datn.service.ChamCongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/cham-cong")
@CrossOrigin("*")
public class ChamCongController {

    private final ChamCongService service;
    private final ChamCongRepository chamCongRepository;

    public ChamCongController(ChamCongService service, ChamCongRepository chamCongRepository) {
        this.service = service;
        this.chamCongRepository = chamCongRepository;
    }

    @PostMapping("/check-in/{idNv}")
    public ResponseEntity<?> checkIn(
            @PathVariable Integer idNv,
            @RequestBody Map<String, Double> payload // Thêm dòng này để nhận JSON
    ){
        try {
            Double tienMat = payload.getOrDefault("tienMatDauCa", 0.0);
            Double tienCk = payload.getOrDefault("tienTaiKhoanDauCa", 0.0);

            // Truyền tiền xuống Service
            return ResponseEntity.ok(service.checkIn(idNv, tienMat, tienCk));
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Đề xuất sửa Backend (Bạn có thể thêm 1 class DTO hoặc dùng Map)

    @PostMapping("/check-out/{idNv}")
    public ResponseEntity<?> checkOut(
            @PathVariable Integer idNv,
            @RequestBody Map<String, Object> payload // Đổi sang Object để nhận cả String (ghi chú)
    ) {
        try {
            Double tienMat = Double.valueOf(payload.getOrDefault("tienMatCuoiCa", 0.0).toString());
            Double tienChuyenKhoan = Double.valueOf(payload.getOrDefault("tienChuyenKhoanCuoiCa", 0.0).toString());
            String ghiChu = (String) payload.getOrDefault("ghiChu", ""); // Hứng ghi chú

            // Truyền xuống Service
            return ResponseEntity.ok(service.checkOut(idNv, tienMat, tienChuyenKhoan, ghiChu));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/hom-nay/{idNv}")
    public ResponseEntity<?> getChamCongHomNay(@PathVariable Integer idNv) {
        try {
            // 👉 ĐẢM BẢO DÒNG NÀY ĐANG GỌI getChamCongHomNay
            ChamCong cc = service.getChamCongHomNay(idNv);

            if (cc == null) {
                return ResponseEntity.ok().build(); // Không có data -> FE sẽ hiểu là mở Check-in
            }
            return ResponseEntity.ok(cc);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // API LẤY DANH SÁCH GIAO CA KẾT TOÁN
    @GetMapping("/giao-ca")
    public ResponseEntity<?> getDanhSachGiaoCa(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            return ResponseEntity.ok(service.getDanhSachGiaoCa(keyword, fromDate, toDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/so-du-ca-truoc")
    public ResponseEntity<?> getSoDuCaTruoc() {
        return ResponseEntity.ok(service.laySoDuCaTruoc());
    }
}